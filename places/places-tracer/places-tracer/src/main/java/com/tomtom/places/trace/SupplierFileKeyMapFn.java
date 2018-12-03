package com.tomtom.places.trace;

import com.cloudera.crunch.MapFn;
import com.cloudera.crunch.Pair;

public class SupplierFileKeyMapFn extends MapFn<Pair<String, Pair<String, Long>>,String> {

	private static final long serialVersionUID = -7344345109528024850L;

	@Override
	public String map(Pair<String, Pair<String, Long>> input) {
		// TODO Auto-generated method stub
		return input.first();
	}


}
