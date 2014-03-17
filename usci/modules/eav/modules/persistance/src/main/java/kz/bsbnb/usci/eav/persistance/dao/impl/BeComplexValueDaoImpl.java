package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;

/**
 * @author a.motov
 */
@Repository
public class BeComplexValueDaoImpl extends JDBCSupport implements IBeComplexValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeComplexValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    @Override
    public void insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        IBaseEntity baseEntity = (IBaseEntity)baseValue.getValue();
        long baseValueId = save(
                baseValue.getBaseContainer().getId(),
                baseValue.getBatch().getId(),
                baseValue.getMetaAttribute().getId(),
                baseValue.getIndex(),
                baseValue.getRepDate(),
                baseEntity.getId(),
                baseValue.isClosed(),
                baseValue.isLast()
        );
        baseValue.setId(baseValueId);
    }

    protected long save(long baseEntityId, long batchId, long metaAttributeId, long index,
                      Date reportDate, long childBaseEntityId, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_VALUES)
                .set(EAV_BE_COMPLEX_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_COMPLEX_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_COMPLEX_VALUES.INDEX_, index)
                .set(EAV_BE_COMPLEX_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID, childBaseEntityId)
                .set(EAV_BE_COMPLEX_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_COMPLEX_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {

    }

    @Override
    public void delete(IPersistable persistable) {

    }
}
