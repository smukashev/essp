package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

public final class DocumentOptimizer {
    private DocumentOptimizer() {
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue noBaseValue = iBaseEntity.getBaseValue("no");
        IBaseValue docTypeBaseValue = iBaseEntity.getBaseValue("doc_type");

        if (noBaseValue == null || docTypeBaseValue == null || noBaseValue.getValue() == null ||
                docTypeBaseValue.getValue() == null)
            throw new IllegalStateException("Документ не содержит обязательные поля; \n" + iBaseEntity);

        IBaseEntity docTypeEntity = (IBaseEntity) docTypeBaseValue.getValue();

        if (docTypeEntity.getId() == 0)
            throw new IllegalStateException("Тип документа не найден; \n" + iBaseEntity);

        stringBuilder.append(noBaseValue.getValue());
        stringBuilder.append("|").append(docTypeEntity.getId());

        return stringBuilder.toString();
    }
}
