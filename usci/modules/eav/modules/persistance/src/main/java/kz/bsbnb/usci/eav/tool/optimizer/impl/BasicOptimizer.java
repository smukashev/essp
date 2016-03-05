package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class BasicOptimizer {
    public static final Set<String> metaList = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(BasicOptimizer.class);

    public static final String META_CREDIT = "credit";
    public static final String META_SUBJECT = "subject";
    public static final String META_DOCUMENT = "document";
    public static final String META_PRIMARY_CONTRACT = "primary_contract";

    static {
        metaList.add(META_SUBJECT);
        metaList.add(META_DOCUMENT);
        metaList.add(META_CREDIT);
        metaList.add(META_PRIMARY_CONTRACT);
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        MetaClass meta = iBaseEntity.getMeta();

        switch (meta.getClassName()) {
            case META_SUBJECT:
                return SubjectOptimizer.getKeyString(iBaseEntity);
            case META_DOCUMENT:
                return DocumentOptimizer.getKeyString(iBaseEntity);
            case META_PRIMARY_CONTRACT:
                return PrimaryContractOptimizer.getKeyString(iBaseEntity);
            case META_CREDIT:
                return CreditOptimizer.getKeyString(iBaseEntity);
            default:
                logger.error(Errors.getError(Errors.getMessage(Errors.E183)) + " : \n" + iBaseEntity);
                throw new IllegalStateException(Errors.getMessage(Errors.E183));
        }
    }
}
