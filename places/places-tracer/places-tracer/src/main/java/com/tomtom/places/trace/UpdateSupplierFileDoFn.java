package com.tomtom.places.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.cloudera.crunch.DoFn;
import com.cloudera.crunch.Emitter;
import com.cloudera.crunch.Pair;
import com.cloudera.crunch.io.text.TextFileReaderFactory;
import com.cloudera.crunch.types.writable.Writables;
import com.google.common.collect.Maps;
import com.tomtom.places.trace.util.SupplierDetails;

public class UpdateSupplierFileDoFn
extends DoFn<Pair<String, Map<String, Collection<Long>>>, Pair<SupplierDetails, Collection<String>>> {

	private static final long serialVersionUID = 5099618211046834091L;
	
	private final Map<String, SupplierDetails> supIdVsSupPojo;

	public UpdateSupplierFileDoFn(Map<String, SupplierDetails> supIdVsSupPojo) {
		this.supIdVsSupPojo=supIdVsSupPojo;
	}

	@Override
	public void process(Pair<String, Map<String, Collection<Long>>> input,
			Emitter<Pair<SupplierDetails, Collection<String>>> emitter) {

		String supplierId = input.first();
		Map<String, Collection<Long>> supDetVsLineNos = input.second();
		SupplierDetails supplierDetails = supIdVsSupPojo.get(supplierId);

		System.out.println("supplierId="+supplierId);
		System.out.println("supplierDetails="+supplierDetails);

		for(Entry<String,Collection<Long>> entry:supDetVsLineNos.entrySet()) {
			Map<String, Collection<Long>> currentEntryMap = Maps.newHashMap();
			
			SupplierDetails deepCopy = new SupplierDetails(supplierDetails);
			String supplierDet = entry.getKey();
			String fileName=supplierDet.substring(supplierDet.lastIndexOf("-")+1);
			currentEntryMap.put(fileName,supDetVsLineNos.get(supplierDet));
			deepCopy.setFileNameVsSetOfLineNumbers(currentEntryMap);
			
			Collection<String> fileredLines=readFileAndFilter(deepCopy);
			emitter.emit(Pair.of(deepCopy, fileredLines));
		}
	}

	private Collection<String> readFileAndFilter(SupplierDetails deepCopy) {
		try {
		Configuration conf= new Configuration();
		
		TextFileReaderFactory<String> sourceFileReader =new TextFileReaderFactory<String>(Writables.strings(), conf);
		
		Entry<String, Collection<Long>> firstEntry = deepCopy.getFileNameVsSetOfLineNumbers().entrySet().iterator().next();
		
		String fileName=firstEntry.getKey();
		String sourceFilePath = deepCopy.getSourceFolderPath()+"/"+fileName;
		
		Collection<Long> lineNumbers=firstEntry.getValue();
		
		Collection<String> filteredLines=new ArrayList<String>();
	    Iterator<String> lineIterator=sourceFileReader.read(FileSystem.get(conf), new Path(sourceFilePath));
	    
	    long cnt=0;
	    while(lineIterator.hasNext()) {
	    	String line = lineIterator.next();
	    	if(cnt==0||lineNumbers.contains(cnt)) {
	    		String updatedLine=upadteLine(cnt,line,deepCopy);
				filteredLines.add(updatedLine);
	    	}
	    	cnt++;
	    }
		
	    return filteredLines;
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String upadteLine(long cnt, String line, SupplierDetails deepCopy) {
		String delimiter = deepCopy.getDelimiter();
		String externalIdFieldName = deepCopy.getExternalIdFieldName();
		
		if(deepCopy.isExtIdMappingPresent()) {
			return updateExistingEntry(cnt, line, delimiter, externalIdFieldName,deepCopy.getExtIdColumnIndx());
		}else {
			return createNewEntry(cnt, new StringBuffer().append(line), delimiter, externalIdFieldName);
		}
	}

	private String updateExistingEntry(long cnt, String line, String delimiter,
			String externalIdFieldName, int extIdColumnIndx) {
		if(cnt==0) {// No need to update header line
			return line;
		}

		StringBuffer updatedLine=new StringBuffer(); 
		
		String[] split = line.split(delimiter);
		for(int i=0;i<split.length;i++) {
			
			String token=split[i];
			if(extIdColumnIndx-1==i) {// Converting index to zero based index
				token=token+UUID.randomUUID().toString();//Update with unique id
			}
		 updatedLine.append(token).append(delimiter);
		}
		
		return updatedLine.toString();
	}

	private String createNewEntry(long cnt, StringBuffer bufferedLine, String delimiter, String externalIdFieldName) {
		String newExtId=UUID.randomUUID().toString();
		if(cnt==0) {
			newExtId=externalIdFieldName;//Add Ext_ID field to header. 
		}
		return bufferedLine.append(delimiter).append(newExtId).toString();
	}

}
