package kz.bsbnb.usci.eav.model.metadata.type.impl;

public class GenericMetaArray<T extends AbstractMetaType> extends AbstractMetaArray {
	T memberType;
	
	public GenericMetaArray(boolean isKey, boolean isNullable) {
		memberType.setKey(isKey);
		memberType.setNullable(isNullable);
	}

	@Override
	public boolean isKey() {
		return memberType.isKey();
	}

	@Override
	public void setKey(boolean isKey) {
		memberType.setKey(isKey);
	}

	@Override
	public boolean isNullable() {
		return memberType.isNullable();
	}

	@Override
	public void setNullable(boolean isNullable) {
		memberType.setNullable(isNullable);
	}

	@Override
	public boolean isComplex() {
		return memberType.isComplex();
	}

}
