package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    private Executor sqlGenerator;

    @Autowired
    private IBeSimpleSetValueDao beSimpleSetValueDao;
    @Autowired
    private IBeComplexSetValueDao beComplexSetValueDao;

    @Override
    public void save(BaseEntity baseEntity, String attribute) {
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

        long setId;
        if (metaType.isComplex())
        {
            setId = beComplexSetValueDao.save(baseValue, metaSet);
        }
        else
        {
            setId = beSimpleSetValueDao.save(baseValue, metaSet);
        }

        InsertOnDuplicateStep insert;
        if (metaType.isSetOfSets())
        {
            insert = sqlGenerator
                    .insertInto(
                            EAV_BE_ENTITY_SET_OF_SETS,
                            EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID,
                            EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID,
                            EAV_BE_ENTITY_SET_OF_SETS.SET_ID,
                            EAV_BE_ENTITY_SET_OF_SETS.IS_LAST)
                    .values(baseEntity.getId(), metaAttribute.getId(), setId, true);
        }
        else
        {
            if (metaType.isComplex())
            {
                insert = sqlGenerator
                        .insertInto(
                                EAV_BE_ENTITY_COMPLEX_SETS,
                                EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID,
                                EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID,
                                EAV_BE_ENTITY_COMPLEX_SETS.SET_ID,
                                EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST)
                        .values(baseEntity.getId(), metaAttribute.getId(), setId, true);
            }
            else
            {
                insert = sqlGenerator
                        .insertInto(
                                EAV_BE_ENTITY_SIMPLE_SETS,
                                EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID,
                                EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID,
                                EAV_BE_ENTITY_SIMPLE_SETS.SET_ID,
                                EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST)
                        .values(baseEntity.getId(), metaAttribute.getId(), setId, true);
            }
        }

        logger.debug(insert.toString());
        insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void save(BaseEntity baseEntity, Set<String> attributes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(BaseEntity baseEntityLoaded, BaseEntity baseEntityForSave, String attribute) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(BaseEntity baseEntity, String attribute) {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        DeleteConditionStep delete;
        if (metaType.isSetOfSets())
        {
            delete = sqlGenerator
                    .delete(EAV_BE_ENTITY_SET_OF_SETS)
                    .where(EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID.eq(baseEntity.getId())
                            .and(EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID.eq(metaAttribute.getId())));
        }
        else
        {
            if (metaType.isComplex())
            {
                delete = sqlGenerator
                        .delete(EAV_BE_ENTITY_COMPLEX_SETS)
                        .where(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID.eq(baseEntity.getId())
                                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(metaAttribute.getId())));
            }
            else
            {
                delete = sqlGenerator
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
    }

    @Override
    public long save(IBaseValue baseValue) {
        InsertOnDuplicateStep insert = sqlGenerator
                .insertInto(
                        EAV_BE_SETS,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .values(baseValue.getBatch().getId(), baseValue.getIndex(), baseValue.getRepDate());

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void remove(BaseSet baseSet) {
        if (baseSet.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BaseSet without id.");
        }

        DeleteConditionStep delete = sqlGenerator
                .delete(EAV_BE_SETS)
                .where(EAV_BE_SETS.ID.eq(baseSet.getId()));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
