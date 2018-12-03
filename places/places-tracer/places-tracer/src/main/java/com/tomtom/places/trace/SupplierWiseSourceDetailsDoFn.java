package com.tomtom.places.trace;

import java.util.List;

import com.cloudera.crunch.DoFn;
import com.cloudera.crunch.Emitter;
import com.cloudera.crunch.Pair;
import com.tomtom.places.unicorn.domain.avro.clustered.ClusteredPlace;
import com.tomtom.places.unicorn.domain.avro.composite.CompositePlace;
import com.tomtom.places.unicorn.domain.avro.trace.Trace;
import com.tomtom.places.unicorn.domain.avro.trace.TraceType;
import com.tomtom.places.unicorn.domain.avro.tracer.PlaceTrace;
import com.tomtom.places.unicorn.matching.rule.MatchPlace;

public class SupplierWiseSourceDetailsDoFn extends DoFn<Pair<String, PlaceTrace>, Pair<String, Pair<String, Long>>> {

	private static final long serialVersionUID = 4340939156661725889L;
	private boolean skipDuplicateRecords;//Removes one of the duplicate that were supplied pre-merging

	public SupplierWiseSourceDetailsDoFn(boolean skipDuplicateRecords) {
		this.skipDuplicateRecords=skipDuplicateRecords;
	}

	@Override
	public void process(Pair<String, PlaceTrace> input, Emitter<Pair<String, Pair<String, Long>>> emitter) {
		// TODO Auto-generated method stub
		
		PlaceTrace placeTrace = input.second();
		
		List<Trace> traces = placeTrace.getTraces();
		for(Trace trace: traces) {
			
			if(trace.getTraceType()	==TraceType.LocalityRule_SourcePlace){
				System.out.println("Trace Params for "+trace.getObjectId()+" ="+trace.getTraceParameters());
				//String supId = trace.getTraceParameters().get("supplier id").toString();
				
				String supId = getSupplierId(placeTrace,trace.getObjectId());
				
				System.out.println("SUP ID for "+trace.getObjectId()+" ="+supId);
				
				String lineNumber= trace.getOriginObjectIds().get(0).toString();
				String supplierInfo = trace.getOriginObjectIds().get(1).toString();
				//String fileName=supplierInfo.substring(supplierInfo.lastIndexOf("-")+1);
				
				emitter.emit(Pair.of(supId, Pair.of(supplierInfo, Long.valueOf(lineNumber))));
				
				//Multiple traces of type 'LocalityRule_SourcePlace' are expected in case PPP merges 2 or more records in Stable Delivery Id phase.
				if(skipDuplicateRecords) {
					break;
				}
			}
		}
	}

	private String getSupplierId(PlaceTrace placeTrace, CharSequence objectId) {
		List<CompositePlace> matchingPlaces = placeTrace.getClusteredPlace().getMatchingPlaces();
		
		for(CompositePlace cp: matchingPlaces) {
			if(cp.getNormalizedPlace().getDeliveryPlaceId().toString().equalsIgnoreCase(objectId.toString())) {
				return cp.getNormalizedPlace().getSupplier().getId().toString();
			}
		}
		return null;
	}

}
