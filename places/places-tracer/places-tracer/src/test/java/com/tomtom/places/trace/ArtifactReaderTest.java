package com.tomtom.places.trace;

import org.junit.Ignore;
import org.junit.Test;

import com.cloudera.crunch.PCollection;
import com.tomtom.places.trace.util.InMemoryPipeline;
import com.tomtom.places.unicorn.domain.avro.clustered.ClusteredPlace;
import com.tomtom.places.unicorn.pipeline.repository.RunDescriptorSupport;
import com.tomtom.places.unicorn.pipelineutil.HdfsTools;

public class ArtifactReaderTest {
	
	private static final String SINGLE_LOCALITY_RUN_DESCRIPTOR_PATH = "src/test/resources/artifact-repository/published/small-run"
	        + "/run-descriptor/run-descriptor-integration-test.xml";
	private final InMemoryPipeline pipeline = new InMemoryPipeline();
	//	private final Pipeline pipeline = new MRPipeline(PlaceTracer.class,HdfsTools.forDefaultFileSystem().getConfiguration());
	
	@Test
	@Ignore	
	public void testReaderNam() throws Exception {
		RunDescriptorSupport rds = RunDescriptorSupport.loadFromFile(HdfsTools.forDefaultFileSystem().getConfiguration(), SINGLE_LOCALITY_RUN_DESCRIPTOR_PATH);
		ArtifactReader reader = new ArtifactReader(SINGLE_LOCALITY_RUN_DESCRIPTOR_PATH, rds, HdfsTools.forDefaultFileSystem());
		
		PCollection<ClusteredPlace> readClusteredPlaces = reader.readClusteredPlaces("CAN+NL", pipeline);
		
		Iterable<ClusteredPlace> materialize = readClusteredPlaces.materialize();
		
		for(ClusteredPlace place:materialize) {
			System.out.println(place);
		}
		
	}
	
	@Test
	public void testReaderNea() throws Exception {
		RunDescriptorSupport rds = RunDescriptorSupport.loadFromFile(HdfsTools.forDefaultFileSystem().getConfiguration(), SINGLE_LOCALITY_RUN_DESCRIPTOR_PATH);
		ArtifactReader reader = new ArtifactReader(SINGLE_LOCALITY_RUN_DESCRIPTOR_PATH, rds, HdfsTools.forDefaultFileSystem());
		
		PCollection<ClusteredPlace> readClusteredPlaces = reader.readClusteredPlaces("HKG", pipeline);
		
		Iterable<ClusteredPlace> materialize = readClusteredPlaces.materialize();
		
		for(ClusteredPlace place:materialize) {
			System.out.println(place);
		}
		
	}

}
