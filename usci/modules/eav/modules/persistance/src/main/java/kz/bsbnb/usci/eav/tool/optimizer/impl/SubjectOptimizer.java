package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SubjectOptimizer {
    private static Logger logger = LoggerFactory.getLogger(SubjectOptimizer.class);

    private static List<String> identificationCodes = new ArrayList<>();

    static {
        identificationCodes.add("06"); // ИНН
        identificationCodes.add("07"); // БИН
        identificationCodes.add("11"); // РНН
        identificationCodes.add("17"); // АИН
        identificationCodes.add("99"); // Присвоенный документ
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue docsBaseValue = iBaseEntity.getBaseValue("docs");

        if (docsBaseValue == null || docsBaseValue.getValue() == null)
            throw new KnownException(Errors.compose(Errors.E188));

        BaseSet docSet = (BaseSet) docsBaseValue.getValue();

        List<IBaseEntity> documents = new ArrayList<>();

        for (IBaseValue docValue : docSet.get()) {
            IBaseEntity document = (IBaseEntity) docValue.getValue();

            IBaseEntity docType = (IBaseEntity) document.getBaseValue("doc_type").getValue();
            String docTypeCode = (String) docType.getBaseValue("code").getValue();

            if (identificationCodes.contains(docTypeCode)) {
                if (document.getId() == 0)
                    return null;

                documents.add(document);
            }
        }

        if (documents.size() == 0)
            throw new KnownException(Errors.compose(Errors.E189));

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
