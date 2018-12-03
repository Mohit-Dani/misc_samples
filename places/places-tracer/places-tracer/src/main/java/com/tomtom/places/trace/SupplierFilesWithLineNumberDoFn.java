package com.tomtom.places.trace;

import java.util.Collection;
import java.util.Map;

import com.cloudera.crunch.DoFn;
import com.cloudera.crunch.Emitter;
import com.cloudera.crunch.Pair;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SupplierFilesWithLineNumberDoFn extends
		DoFn<Pair<String, Iterable<Pair<String, Pair<String, Long>>>>, Pair<String, Map<String, Collection<Long>>>> {

	private static final long serialVersionUID = 2335136073740909382L;

	@Override
	public void process(Pair<String, Iterable<Pair<String, Pair<String, Long>>>> input,
			Emitter<Pair<String, Map<String, Collection<Long>>>> emitter) {
		// TODO Auto-generated method stub
		
		 Map<String, Collection<Long>> elements = Maps.newHashMap();
		 
		 for (Pair<String, Pair<String, Long>> nestedCollection : input.second()) {
			 
			 Pair<String, Long> fileNameLineNoPair = nestedCollection.second();
			 String fileName = fileNameLineNoPair.first();
			 Long lineNumber = fileNameLineNoPair.second();
			 
			Collection<Long> lineNumbers = elements.get(fileName);
			 if(lineNumbers==null) {
				 lineNumbers=Sets.newHashSet();
				 elements.put(fileName, lineNumbers);
			 }
			lineNumbers.add(lineNumber);
	        }
		 
		 emitter.emit(Pair.of(input.first(), elements));

	}
}
