package kz.bsbnb.usci.eav.model.metadata.type.impl;

public class GenericMetaArray<T extends AbstractMetaType> extends AbstractMetaArray {
	T memberType;

    GenericMetaArray(T memberType) {
        this.memberType = memberType;
    }

	GenericMetaArray(T memberType, boolean isKey, boolean isNullable) {
        this.memberType = memberType;
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

    public T getMembersType() {
        return memberType;
    }

    public void setMembersType(T memberType) {
        this.memberType = memberType;
    }
}
