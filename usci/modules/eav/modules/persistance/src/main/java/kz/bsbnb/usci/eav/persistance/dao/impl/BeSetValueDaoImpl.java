package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_SETS;

/**
 *
 */
@Repository
public class BeSetValueDaoImpl extends JDBCSupport implements IBeSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IBeSimpleSetValueDao beSimpleSetValueDao;
    @Autowired
    private IBeComplexSetValueDao beComplexSetValueDao;

    @Override
    public long save(final IBaseEntity baseEntity, String attribute) {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();
        MetaSet metaSet = (MetaSet)metaType;

        if (metaAttribute.getId() < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain an id. " +
                    "The set can not be saved.");
        }

        IBaseValue baseValue = baseEntity.getBaseValue(attribute);
        IBaseSet baseSet = (IBaseSet)baseValue.getValue();
        long setId;
        if (metaType.isComplex())
        {
            setId = beComplexSetValueDao.save(baseValue, metaSet);
        }
        else
        {
            setId = beSimpleSetValueDao.save(baseValue, metaSet);
        }
        baseSet.setId(setId);

        InsertOnDuplicateStep insert;
        if (metaType.isSetOfSets())
        {
            insert = context
                    .insertInto(EAV_BE_ENTITY_SET_OF_SETS)
                    .set(EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID, baseEntity.getId())
                    .set(EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID, metaAttribute.getId())
                    .set(EAV_BE_ENTITY_SET_OF_SETS.SET_ID, baseSet.getId())
                    .set(EAV_BE_ENTITY_SET_OF_SETS.BATCH_ID, baseValue.getBatch().getId())
                    .set(EAV_BE_ENTITY_SET_OF_SETS.INDEX_, baseValue.getIndex())
                    .set(EAV_BE_ENTITY_SET_OF_SETS.REPORT_DATE, DateUtils.convert(baseEntity.getReportDate()))
                    .set(EAV_BE_ENTITY_SET_OF_SETS.IS_CLOSED, false)
                    .set(EAV_BE_ENTITY_SET_OF_SETS.IS_LAST, true);
        }
        else
        {
            if (metaType.isComplex())
            {
                insert = context
                        .insertInto(EAV_BE_ENTITY_COMPLEX_SETS)
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID, baseEntity.getId())
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID, metaAttribute.getId())
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID, baseSet.getId())
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_, baseValue.getIndex())
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE, DateUtils.convert(baseEntity.getReportDate()))
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED, false)
                        .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST, true);
            }
            else
            {
                insert = context
                        .insertInto(EAV_BE_ENTITY_SIMPLE_SETS)
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID, baseEntity.getId())
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID, metaAttribute.getId())
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID, baseSet.getId())
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_, baseValue.getIndex())
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE, DateUtils.convert(baseEntity.getReportDate()))
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED, false)
                        .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST, true);
            }
        }

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void save(final IBaseEntity baseEntity, Set<String> attributes) {
        Iterator<String> it = attributes.iterator();
        while(it.hasNext())
        {
            String attribute = it.next();
            save(baseEntity, attribute);
        }
    }

    @Override
    public void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void remove(final IBaseEntity baseEntity, String attribute) {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        DeleteConditionStep delete;
        if (metaType.isSetOfSets())
        {
            delete = context
                    .delete(EAV_BE_ENTITY_SET_OF_SETS)
                    .where(EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID.eq(baseEntity.getId())
                            .and(EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID.eq(metaAttribute.getId())));
        }
        else
        {
            if (metaType.isComplex())
            {
                delete = context
                        .delete(EAV_BE_ENTITY_COMPLEX_SETS)
                        .where(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID.eq(baseEntity.getId())
                                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(metaAttribute.getId())));
            }
            else
            {
                delete = context
                        .delete(EAV_BE_ENTITY_SIMPLE_SETS)
                        .where(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID.eq(baseEntity.getId())
                                .and(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.eq(metaAttribute.getId())));
            }
        }

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        BaseSet baseSet = (BaseSet)baseEntity.getBaseValue(attribute).getValue();
        if (metaType.isComplex())
        {
            beComplexSetValueDao.remove(baseSet);
        }
        else
        {
            beSimpleSetValueDao.remove(baseSet);
        }

        baseEntity.remove(attribute);
    }

    @Override
    public long save(IBaseSet baseSet) {
        //TODO: Remove temp field
        Insert insert = context
                .insertInto(EAV_BE_SETS)
                .set(EAV_BE_SETS.TEMP, new Long("0"));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void remove(IBaseSet baseSet) {
        if (baseSet.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BaseSet without id.");
        }

        DeleteConditionStep delete = context
                .delete(EAV_BE_SETS)
                .where(EAV_BE_SETS.ID.eq(baseSet.getId()));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
