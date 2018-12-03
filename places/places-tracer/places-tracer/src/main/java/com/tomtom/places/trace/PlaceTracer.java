package com.tomtom.places.trace;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.cloudera.crunch.PCollection;
import com.cloudera.crunch.PGroupedTable;
import com.cloudera.crunch.PTable;
import com.cloudera.crunch.Pair;
import com.cloudera.crunch.Pipeline;
import com.cloudera.crunch.PipelineResult;
import com.cloudera.crunch.Target;
import com.cloudera.crunch.fn.IdentityFn;
import com.cloudera.crunch.fn.PairMapFn;
import com.cloudera.crunch.impl.mr.MRPipeline;
import com.cloudera.crunch.io.avro.AvroFileTarget;
import com.cloudera.crunch.types.PType;
import com.cloudera.crunch.types.avro.AvroType;
import com.cloudera.crunch.types.avro.Avros;
import com.cloudera.crunch.types.writable.Writables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tomtom.places.trace.util.SupplierConfigrationReaderUtil;
import com.tomtom.places.trace.util.SupplierDetails;
import com.tomtom.places.unicorn.configuration.domain.Supplier;
import com.tomtom.places.unicorn.configuration.domain.SupplierColumnDefinition;
import com.tomtom.places.unicorn.configuration.domain.SupplierFieldLink;
import com.tomtom.places.unicorn.configuration.service.Artifacts;
import com.tomtom.places.unicorn.configuration.service.ConfigFileOpener;
import com.tomtom.places.unicorn.configuration.service.DefaultConfigFileOpener;
import com.tomtom.places.unicorn.configuration.service.reader.CsvMappableAttributeReader;
import com.tomtom.places.unicorn.configuration.service.reader.CsvSupplierReader;
import com.tomtom.places.unicorn.domain.avro.tracer.PlaceTrace;
import com.tomtom.places.unicorn.pipeline.repository.RunDescriptorSupport;
import com.tomtom.places.unicorn.pipelineutil.HdfsTools;
import com.tomtom.places.unicorn.rundescriptor.ArtifactId;

public class PlaceTracer extends Configured implements Tool {

	private static final Logger LOGGER = Logger.getLogger(PlaceTracer.class);

	private final String runDescriptorPath;
	private final RunDescriptorSupport rds;
	private final ArtifactReader reader;

	public PlaceTracer(String runDescriptorPath) throws Exception {
		this(runDescriptorPath, HdfsTools.forDefaultFileSystem().getConfiguration());
	}

	public PlaceTracer(String runDescriptorPath, Configuration conf) throws Exception {
		super(conf);
		this.runDescriptorPath = runDescriptorPath;
		rds = RunDescriptorSupport.loadFromFile(conf, runDescriptorPath);
		reader = new ArtifactReader(runDescriptorPath, rds, HdfsTools.forDefaultFileSystem());
	}

	public static void main(String[] args) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		ToolRunner.run(new Configuration(), new PlaceTracer(args[0]), args);
		LOGGER.info("Finished in: " + DurationFormatUtils.formatDuration(watch.getTime(), "HH 'hr', mm 'min', ss 'sec'"));
	}

	public int run(String[] args) throws Exception {
		Pipeline pipeline = new MRPipeline(PlaceTracer.class, getConf());
		PipelineResult result = run(pipeline);
		int status = result.succeeded() ? 0 : -1;
		LOGGER.info("Pipeline status:" + status);
		return status;
	}

	public PipelineResult run(Pipeline pipeline) throws Exception {
		String mappedPlaces = rds.getOutputArtifactPath(ArtifactId.MAPPED_PLACES, true);
		List<String> dirs = HdfsTools.forDefaultFileSystem().listDirectories(mappedPlaces, ".*");
		for (String locality : dirs) {
			writePlaceTraces(locality, pipeline, createPlaceTraces(locality, pipeline));
		}
		return pipeline.done();
	}

	@SuppressWarnings("unchecked")
	public PTable<SupplierDetails, Collection<String>> createPlaceTraces(String locality, Pipeline pipeline) throws Exception {
		ArtifactWrapperWithKey wrapper = new ArtifactWrapperWithKey(runDescriptorPath, rds, HdfsTools.forDefaultFileSystem());
		PCollection<Pair<String, PlaceTrace>> artifacts = wrapper.getAllArtifacts(locality, pipeline);
		PGroupedTable<String, Pair<String, PlaceTrace>> groupByKey = artifacts.by(new PlaceKeyFn(), Avros.strings()).groupByKey();

		//  PCollection<PlaceTrace> traces = groupByKey.parallelDo(new PlaceTracerDoFn(), Avros.records(PlaceTrace.class));
		PCollection<Pair<String, PlaceTrace>> filteredTraces = groupByKey.parallelDo(new FilteredPlaceTracerDoFn(false), Avros.pairs(Avros.strings(),Avros.records(PlaceTrace.class)));


		//  PCollection<Pair<Supplier,Pair<FileName,LineNumber >>>
		PCollection<Pair<String,Pair<String,Long>>> supplierWiseFileDetails=filteredTraces.parallelDo(new SupplierWiseSourceDetailsDoFn(true), Avros.pairs(Avros.strings(), Avros.pairs(Avros.strings(), Avros.longs())));

		//Approach::1 =>

		/**
		IdentityFn<Pair<String,Pair<String,String>>> identity = IdentityFn.getInstance();
		PTable<String, Pair<String, String>> pTable = supplierWiseFileDetails.parallelDo(identity,Avros.tableOf(Avros.strings(), Avros.pairs(Avros.strings(), Avros.strings())));
		PGroupedTable<String, Pair<String, String>> groupByKeyPTable = pTable.groupByKey();
		PTable<String, Map<String,Collection<String>>> supplierFilesWithLineNumbers2=groupByKeyPTable.parallelDo(new SupplierFilesWithLineNumberDoFn2(), Avros.tableOf(Avros.strings(), Avros.maps(Avros.collections(Avros.strings()))));
		 */

		//Approach::2 =>

		/**
		IdentityFn<String> identityKey = IdentityFn.getInstance();
		IdentityFn<Pair<String,String>> identityValue = IdentityFn.getInstance();
		PTable<String, Pair<String, String>> pTable = supplierWiseFileDetails.parallelDo(new PairMapFn<String,Pair<String,String>,String,Pair<String,String>>(identityKey,identityValue),Avros.tableOf(Avros.strings(), Avros.pairs(Avros.strings(), Avros.strings())));
		PGroupedTable<String, Pair<String, String>> groupByKeyPTable = pTable.groupByKey();
		PTable<String, Map<String,Collection<String>>> supplierFilesWithLineNumbers2=groupByKeyPTable.parallelDo(new SupplierFilesWithLineNumberDoFn2(), Avros.tableOf(Avros.strings(), Avros.maps(Avros.collections(Avros.strings()))));
		 */ 



		//Approach::3 => Tested

		PGroupedTable<String, Pair<String, Pair<String, Long>>> groupBySupplier = supplierWiseFileDetails.by(new SupplierFileKeyMapFn(), Avros.strings()).groupByKey();
		//  PCollection<Supplier,Map<FileName,Set<LineNumber> >>
		PTable<String, Map<String,Collection<Long>>> supplierFilesWithLineNumbers=groupBySupplier.parallelDo(new SupplierFilesWithLineNumberDoFn(), Avros.tableOf(Avros.strings(), Avros.maps(Avros.collections(Avros.longs()))));


		Map<String,SupplierDetails> supIdVsSupPojo=SupplierConfigrationReaderUtil.readSupplierConfiguration(rds);
		readAndUpdateSourceFolderPath(pipeline,rds,supIdVsSupPojo);


		PType<SupplierDetails> supplierDetailsPType = Avros.reflects(SupplierDetails.class);
		
		PTable<SupplierDetails,Collection<String>> supplierVsFileContent=supplierFilesWithLineNumbers.parallelDo(new UpdateSupplierFileDoFn(supIdVsSupPojo), Avros.tableOf(supplierDetailsPType, Avros.collections(Avros.strings())));

		//supplierVsFileContent.parallelDo(new WriteFilteredLines(), supplierDetailsPType);

		return supplierVsFileContent;

		// TODO: gather traces logged with ClusterPlaceID and/or ArchivePlaceID
		// 1) emit traced collection as Pair<ClusteredPlaceId, PlaceTrace>
		// 2) read and emit archive places as Pair<ArchivePlaceId, Wrap ArchivePlace into PlaceTrace>
		// 3) how to emit and combine traces that were logged with ClusteredPlaceId?
		// union 1, 2 & 3 and groupByKey() then in process wrap all places together into PlaceTrace and emit wrapped PlaceTrace
	}

	

	private <T>  void writePlaceTraces(String locality, Pipeline pipeline, PCollection<T> traced) throws IOException {
		String placeTracesPath =
				runDescriptorPath.substring(0, runDescriptorPath.indexOf("run-descriptor") - 1) + "/place-traces/" + locality;
		HdfsTools.forDefaultFileSystem().mkdirs(placeTracesPath);
		LOGGER.info("Writing place traces in: " + placeTracesPath);
		Target target = new AvroFileTarget(placeTracesPath);
		pipeline.write(traced, target);
	}
	
	
	public static Map<String, String> readAndUpdateSourceFolderPath(Pipeline pipeline,RunDescriptorSupport rds, Map<String, SupplierDetails> supIdVsSupPojoMap) {
		// TODO Auto-generated method stub
		Map<String,String> suplierIdVsNames=Maps.newHashMap();
		for(Entry<String,SupplierDetails> supIdVsSupDetPojo:supIdVsSupPojoMap.entrySet()) {
			suplierIdVsNames.put(supIdVsSupDetPojo.getKey(),supIdVsSupDetPojo.getValue().getSupplierName());
		}
		
		
		String intakeDeliveryListPath = rds.getIntakeDeliveryListPath();
		
		PCollection<String> readTextFile = pipeline.readTextFile(intakeDeliveryListPath);
		Iterable<String> lineIterator = readTextFile.materialize();

		
		HashMap<String, String> supNameVsSourceFolderPath = Maps.newHashMap();
		for(String line:lineIterator) {
			
			for(Entry<String,String> suplierIdVsName:suplierIdVsNames.entrySet()) {
				if(line.contains("/"+suplierIdVsName.getValue())) {
					supIdVsSupPojoMap.get(suplierIdVsName.getKey()).setSourceFolderPath(line);
					
					supNameVsSourceFolderPath.put(suplierIdVsName.getValue(), line);
					break;
				}
			}
		}
		return supNameVsSourceFolderPath;
	}
}
