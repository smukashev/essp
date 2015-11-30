package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.tool.optimizer.IEavOptimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SubjectOptimizer implements IEavOptimizer {
    @Override
    public String getMetaName() {
        return "subject";
    }

    @Override
    public String getKeyString(IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        if (iBaseEntity == null)
            throw new IllegalStateException("keyString не может применяться на пустые обьекты;");

        IBaseValue docsBaseValue = iBaseEntity.getBaseValue("docs");

        if (docsBaseValue == null || docsBaseValue.getValue() == null)
            throw new IllegalStateException("Ключевое поле docs пустое;");

        BaseSet docSet = (BaseSet) docsBaseValue.getValue();

        List<IBaseEntity> documents = new ArrayList<>();

        for (IBaseValue docValue : docSet.get()) {
            IBaseEntity document = (IBaseEntity) docValue.getValue();

            IBaseEntity docType = (IBaseEntity) document.getBaseValue("doc_type").getValue();
            boolean isIdentification = (boolean) docType.getBaseValue("is_identification").getValue();

            if (isIdentification)
                documents.add(document);
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
                stringBuilder.append("|");

            stringBuilder.append(new BasicOptimizer().getKeyString(tmpEntity));
        }

        return stringBuilder.toString();
    }
}
