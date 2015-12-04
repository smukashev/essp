package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SubjectOptimizer {
    private SubjectOptimizer() {
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue docsBaseValue = iBaseEntity.getBaseValue("docs");

        if (docsBaseValue == null || docsBaseValue.getValue() == null)
            throw new IllegalStateException("Ключевое поле docs пустое; \n" + iBaseEntity);

        BaseSet docSet = (BaseSet) docsBaseValue.getValue();

        List<IBaseEntity> documents = new ArrayList<>();

        for (IBaseValue docValue : docSet.get()) {
            IBaseEntity document = (IBaseEntity) docValue.getValue();

            IBaseEntity docType = (IBaseEntity) document.getBaseValue("doc_type").getValue();
            boolean isIdentification = (boolean) docType.getBaseValue("is_identification").getValue();

            if (isIdentification) {
                if (document.getId() == 0)
                    return null;

                documents.add(document);
            }
        }

        if (documents.size() == 0)
            throw new IllegalStateException("Субъект должен иметь идентификационные документы; \n" + iBaseEntity);

        Collections.sort(documents, new Comparator<IBaseEntity>() {
            @Override
            public int compare(IBaseEntity o1, IBaseEntity o2) {
                return o1.getId() == o2.getId() ? 0 : o1.getId() >= o2.getId() ? 1 : -1;
            }
        });

        for (IBaseEntity tmpEntity : documents) {
            if (stringBuilder.length() > 0)
                stringBuilder.append(",");

            stringBuilder.append(tmpEntity.getId());
        }

        return stringBuilder.toString();
    }
}
