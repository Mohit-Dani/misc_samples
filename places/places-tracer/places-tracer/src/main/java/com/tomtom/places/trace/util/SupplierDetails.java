package com.tomtom.places.trace.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SupplierDetails implements Serializable{
	
	private static final long serialVersionUID = -6385417610750101518L;

	private String supplierId;
	private String supplierName;
	private String delimiter;
	private String fileEncoding;
	
	private boolean isExtIdMappingPresent=false;
	private String externalIdFieldName;
	// 1 based index i.e. index starts with 1 
	private int extIdColumnIndx;
	
	private String fieldMappingEntry;
	private String fieldLinkMappingEntry;
	private String sourceFolderPath;
	
	private Map<String,Collection<Long>> fileNameVsSetOfLineNumbers;
	
	
	
	public SupplierDetails() {
	}
	
	public SupplierDetails(String supplierId, String supplierName, String delimiter, String fileEncoding,
			boolean isExtIdMappingPresent, String externalIdFieldName, int extIdColumnIndx, String fieldMappingEntry,
			String fieldLinkMappingEntry,String sourceFilesFolder, Map<String, Collection<Long>> fileNameVsSetOfLineNumbers,String sourceFolderPath) {
		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.delimiter = delimiter;
		this.fileEncoding = fileEncoding;
		this.isExtIdMappingPresent = isExtIdMappingPresent;
		this.externalIdFieldName = externalIdFieldName;
		this.extIdColumnIndx = extIdColumnIndx;
		this.fieldMappingEntry = fieldMappingEntry;
		this.fieldLinkMappingEntry = fieldLinkMappingEntry;
		this.fileNameVsSetOfLineNumbers = fileNameVsSetOfLineNumbers;
		this.sourceFolderPath=sourceFolderPath;
	}
	

	public SupplierDetails(SupplierDetails supplierDetails) {
		this.supplierId = supplierDetails.supplierId;
		this.supplierName = supplierDetails.supplierName;
		this.delimiter = supplierDetails.delimiter;
		this.fileEncoding = supplierDetails.fileEncoding;
		this.isExtIdMappingPresent = supplierDetails.isExtIdMappingPresent;
		this.externalIdFieldName = supplierDetails.externalIdFieldName;
		this.extIdColumnIndx = supplierDetails.extIdColumnIndx;
		this.fieldMappingEntry = supplierDetails.fieldMappingEntry;
		this.fieldLinkMappingEntry = supplierDetails.fieldLinkMappingEntry;
		this.sourceFolderPath=supplierDetails.sourceFolderPath;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	public boolean isExtIdMappingPresent() {
		return isExtIdMappingPresent;
	}

	public void setExtIdMappingPresent(boolean isExtIdMappingPresent) {
		this.isExtIdMappingPresent = isExtIdMappingPresent;
	}

	public String getExternalIdFieldName() {
		return externalIdFieldName;
	}

	public void setExternalIdFieldName(String externalIdFieldName) {
		this.externalIdFieldName = externalIdFieldName;
	}

	public int getExtIdColumnIndx() {
		return extIdColumnIndx;
	}

	public void setExtIdColumnIndx(int extIdColumnIndx) {
		this.extIdColumnIndx = extIdColumnIndx;
	}

	public String getFieldMappingEntry() {
		return fieldMappingEntry;
	}

	public void setFieldMappingEntry(String fieldMappingEntry) {
		this.fieldMappingEntry = fieldMappingEntry;
	}

	public String getFieldLinkMappingEntry() {
		return fieldLinkMappingEntry;
	}

	public void setFieldLinkMappingEntry(String fieldLinkMappingEntry) {
		this.fieldLinkMappingEntry = fieldLinkMappingEntry;
	}

	public Map<String, Collection<Long>> getFileNameVsSetOfLineNumbers() {
		return fileNameVsSetOfLineNumbers;
	}

	public void setFileNameVsSetOfLineNumbers(Map<String, Collection<Long>> fileNameVsSetOfLineNumbers) {
		this.fileNameVsSetOfLineNumbers = fileNameVsSetOfLineNumbers;
	}

	public String getSourceFolderPath() {
		return this.sourceFolderPath;
	}

	public void setSourceFolderPath(String sourceFolderPath) {
		this.sourceFolderPath = sourceFolderPath;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SupplierDetails [supplierId=").append(supplierId).append(", supplierName=").append(supplierName)
				.append(", delimiter=").append(delimiter).append(", fileEncoding=").append(fileEncoding)
				.append(", isExtIdMappingPresent=").append(isExtIdMappingPresent).append(", externalIdFieldName=")
				.append(externalIdFieldName).append(", extIdColumnIndx=").append(extIdColumnIndx)
				.append(", fieldMappingEntry=").append(fieldMappingEntry).append(", fieldLinkMappingEntry=")
				.append(fieldLinkMappingEntry).append(", fileNameVsSetOfLineNumbers=")
				.append(fileNameVsSetOfLineNumbers).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delimiter == null) ? 0 : delimiter.hashCode());
		result = prime * result + extIdColumnIndx;
		result = prime * result + ((externalIdFieldName == null) ? 0 : externalIdFieldName.hashCode());
		result = prime * result + ((fieldLinkMappingEntry == null) ? 0 : fieldLinkMappingEntry.hashCode());
		result = prime * result + ((fieldMappingEntry == null) ? 0 : fieldMappingEntry.hashCode());
		result = prime * result + ((fileEncoding == null) ? 0 : fileEncoding.hashCode());
		result = prime * result + ((fileNameVsSetOfLineNumbers == null) ? 0 : fileNameVsSetOfLineNumbers.hashCode());
		result = prime * result + (isExtIdMappingPresent ? 1231 : 1237);
		result = prime * result + ((supplierId == null) ? 0 : supplierId.hashCode());
		result = prime * result + ((supplierName == null) ? 0 : supplierName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SupplierDetails other = (SupplierDetails) obj;
		if (delimiter == null) {
			if (other.delimiter != null)
				return false;
		} else if (!delimiter.equals(other.delimiter))
			return false;
		if (extIdColumnIndx != other.extIdColumnIndx)
			return false;
		if (externalIdFieldName == null) {
			if (other.externalIdFieldName != null)
				return false;
		} else if (!externalIdFieldName.equals(other.externalIdFieldName))
			return false;
		if (fieldLinkMappingEntry == null) {
			if (other.fieldLinkMappingEntry != null)
				return false;
		} else if (!fieldLinkMappingEntry.equals(other.fieldLinkMappingEntry))
			return false;
		if (fieldMappingEntry == null) {
			if (other.fieldMappingEntry != null)
				return false;
		} else if (!fieldMappingEntry.equals(other.fieldMappingEntry))
			return false;
		if (fileEncoding == null) {
			if (other.fileEncoding != null)
				return false;
		} else if (!fileEncoding.equals(other.fileEncoding))
			return false;
		if (fileNameVsSetOfLineNumbers == null) {
			if (other.fileNameVsSetOfLineNumbers != null)
				return false;
		} else if (!fileNameVsSetOfLineNumbers.equals(other.fileNameVsSetOfLineNumbers))
			return false;
		if (isExtIdMappingPresent != other.isExtIdMappingPresent)
			return false;
		if (supplierId == null) {
			if (other.supplierId != null)
				return false;
		} else if (!supplierId.equals(other.supplierId))
			return false;
		if (supplierName == null) {
			if (other.supplierName != null)
				return false;
		} else if (!supplierName.equals(other.supplierName))
			return false;
		return true;
	}
}