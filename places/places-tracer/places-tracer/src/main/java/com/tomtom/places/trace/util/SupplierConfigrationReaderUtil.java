package com.tomtom.places.trace.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.tomtom.places.unicorn.configuration.domain.Supplier;
import com.tomtom.places.unicorn.configuration.domain.SupplierColumnDefinition;
import com.tomtom.places.unicorn.configuration.domain.SupplierFieldLink;
import com.tomtom.places.unicorn.configuration.service.Artifacts;
import com.tomtom.places.unicorn.configuration.service.ConfigFileOpener;
import com.tomtom.places.unicorn.configuration.service.DefaultConfigFileOpener;
import com.tomtom.places.unicorn.configuration.service.reader.CsvMappableAttributeReader;
import com.tomtom.places.unicorn.configuration.service.reader.CsvSupplierReader;
import com.tomtom.places.unicorn.pipeline.repository.RunDescriptorSupport;

public class SupplierConfigrationReaderUtil {
	private static final String GLOBAL_CONFIG_PATH = "LoadData-ALL";
	private static final String GROUP_NUM_VAL = "1";
	private static final String NOT_STABLE = "NotStable";
	private static final String SUP_EXTERNAL_ID = "SUP_EXTERNAL_ID";
	private static final String EXTERNAL_ID_VALUE_ATTR = "ExternalId.Value";
	private static final String PIPE="|";
	


	// Field.csv =>	
	// SUPPLIERID|POSITION|WIDTH|NAME|FIELDID

	// Fieldlink.csv
	// SUPPLIERID|ATTNAME|ATTSUBTYPE|GROUPNUM|SEQNUM|SUPPLIERFIELD|BEGIN|WIDTH|VALUE
	
	// attribute.csv
	// TYPE|DATATYPE|QGROUP|NAME|FLAGS|DESCRIPTION

	// supplier.csv
	// SUPPLIERID|NAME|ABBREV|FORMAT|ENCODING|DELIMITER|COUNTRY|FLAGS|RECOGNIZER|RECELEMENT|DESCRIPTION
    
	
	public static Map<String, SupplierDetails> readSupplierConfiguration(RunDescriptorSupport runDescriptorSupport) {
		Map<String,SupplierDetails> supIdVsSupplierDetails=Maps.newHashMap();
		
		
		Artifacts artifacts = readSupplierRelatedConfiguration(runDescriptorSupport);

		Map<Long, Supplier> suppliers = artifacts.getSuppliers();
		for(Entry<Long,Supplier> entry:suppliers.entrySet()) {

			Supplier supplier = entry.getValue();

			SupplierDetails supplierDetails = new SupplierDetails();
			supplierDetails.setSupplierId(entry.getKey().toString());
			supplierDetails.setSupplierName(supplier.getAbbreviation());
			supplierDetails.setDelimiter(String.valueOf(supplier.getDelimiter()));
			supplierDetails.setFileEncoding(supplier.getFileEncoding());

			updateSupplierDetailsWithExtIdFieldMapping(supplier, supplierDetails);

			supIdVsSupplierDetails.put(supplierDetails.getSupplierId(), supplierDetails);
		}
		return supIdVsSupplierDetails;
	}

	private static Artifacts readSupplierRelatedConfiguration(RunDescriptorSupport rds) {
		Artifacts artifacts=new Artifacts();
		
		String gp3Config = rds.getConfigurationArchiveDistributedCacheHdfsPath();
		String locality = rds.getConfigurationApiLocalPath();
		
		ConfigFileOpener opener=new DefaultConfigFileOpener(rds.getConfigurationArchiveDistributedCacheSymlink());
		new CsvMappableAttributeReader(opener, GLOBAL_CONFIG_PATH, artifacts);
		new CsvSupplierReader(opener, locality, artifacts);
		return artifacts;
	}

	private static void updateSupplierDetailsWithExtIdFieldMapping(Supplier supplier, SupplierDetails supplierDetails) {
		Integer lastIndex=0;
		List<SupplierFieldLink> fieldLinks = supplier.getFieldLinks();
		
		for(SupplierFieldLink fieldLink:fieldLinks) {
			Integer columnIndex = supplier.getColumnIndex(fieldLink.getSupplierFieldName());

			if(fieldLink.getMappableAttribute().getName().equals(EXTERNAL_ID_VALUE_ATTR)){
				supplierDetails.setExtIdMappingPresent(true);

				getExistingExternalIdMapping(supplier, supplierDetails, fieldLink, columnIndex);
				break;
			}

			//Need if no entry is present
			if(columnIndex>lastIndex) {
				lastIndex=columnIndex;
			}
		}

		if(!supplierDetails.isExtIdMappingPresent()) {

			//Create new field with index
			createNewExtIdMapping(supplierDetails, lastIndex);
		}
	}

	private static void getExistingExternalIdMapping(Supplier supplier, SupplierDetails supplierDetails,
			SupplierFieldLink fieldLink, Integer columnIndex) {
		supplierDetails.setExternalIdFieldName(fieldLink.getSupplierFieldName());
		supplierDetails.setExtIdColumnIndx(columnIndex);

		supplierDetails.setFieldLinkMappingEntry(createFieldLinkEntry(supplierDetails, fieldLink));
		
		Collection<SupplierColumnDefinition> definedColumns = supplier.getDefinedColumns();
		for(SupplierColumnDefinition scd:definedColumns) {
			if(scd.getName().equals(supplierDetails.getExternalIdFieldName())) {
				supplierDetails.setFieldLinkMappingEntry(createFieldEntry(supplierDetails, scd));
			}
		}
	}

	private static void createNewExtIdMapping(SupplierDetails supplierDetails, int lastIndex) {
		supplierDetails.setExternalIdFieldName(SUP_EXTERNAL_ID);
		supplierDetails.setExtIdColumnIndx(lastIndex+1);

		supplierDetails.setFieldLinkMappingEntry(createNewFieldLinkEntry(supplierDetails));
		supplierDetails.setFieldLinkMappingEntry(createNewFieldEntry(supplierDetails));
	}

	private static String createFieldEntry(SupplierDetails supplierDetails, SupplierColumnDefinition scd) {
		return createFieldEntry(supplierDetails.getSupplierId(),scd.getPosition(),scd.getWidth(),
				scd.getName(),scd.getIndex());
	}

	private static String createNewFieldEntry(SupplierDetails supplierDetails) {
		return createFieldEntry(supplierDetails.getSupplierId(),supplierDetails.getExtIdColumnIndx(),null,
				supplierDetails.getExternalIdFieldName(),supplierDetails.getExtIdColumnIndx());
	}

	private static String createFieldEntry(String SUPPLIERID,Integer POSITION,Integer WIDTH,String NAME,Integer FIELDID) {
		return new StringBuffer().append(SUPPLIERID).append(PIPE)
				.append(emptyIfNull(POSITION)).append(PIPE)
				.append(emptyIfNull(WIDTH)).append(PIPE)
				.append(NAME).append(PIPE)
				.append(emptyIfNull(FIELDID)).append(PIPE).toString();
	}

	private static String createFieldLinkEntry(SupplierDetails supplierDetails, SupplierFieldLink fieldLink) {
		return createFieldLinkEntry(supplierDetails.getSupplierId(),EXTERNAL_ID_VALUE_ATTR,fieldLink.getSubTypeValue()
				,fieldLink.getGroupId(),fieldLink.getSequence(),fieldLink.getSupplierFieldName()
				,fieldLink.getOffset(),fieldLink.getWidth(),fieldLink.getLiteralValue());
	}

	private static String createNewFieldLinkEntry(SupplierDetails supplierDetails) {
		return createFieldLinkEntry(supplierDetails.getSupplierId(),EXTERNAL_ID_VALUE_ATTR,NOT_STABLE
				,GROUP_NUM_VAL,null,supplierDetails.getExternalIdFieldName()
				,null,null,null);
	}


	private static String createFieldLinkEntry(String SUPPLIERID,String ATTNAME,String ATTSUBTYPE,String GROUPNUM,Integer SEQNUM,String SUPPLIERFIELD,Integer BEGIN,Integer WIDTH,String VALUE) {
		return new StringBuffer().append(SUPPLIERID).append(PIPE).append(ATTNAME).append(PIPE)
				.append(ATTSUBTYPE).append(PIPE).append(GROUPNUM).append(PIPE)
				.append(emptyIfNull(SEQNUM)).append(PIPE).append(SUPPLIERFIELD).append(PIPE)
				.append(emptyIfNull(BEGIN)).append(PIPE).append(emptyIfNull(WIDTH)).append(PIPE)
				.append(emptyIfNull(VALUE)).toString();
	}

	private static String emptyIfNull(Object obj) {
		return obj==null?"":obj.toString();
	}
}