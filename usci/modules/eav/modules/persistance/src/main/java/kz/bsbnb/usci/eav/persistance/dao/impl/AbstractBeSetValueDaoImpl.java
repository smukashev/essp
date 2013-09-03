package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_SETS;

/**
 *
 */
@Repository
public abstract class AbstractBeSetValueDaoImpl extends JDBCSupport implements IBeSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBaseEntityDao baseEntityDao;

    public static final long INITIAL_LEVEL = 1;

    @Override
    public void save(final IBaseEntity baseEntity, String attribute) {
        IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
        if (metaAttribute.getId() < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain an id. " +
                    "The set can not be saved.");
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (!metaType.isSet())
        {
            throw new IllegalArgumentException("");
        }

        IBaseValue baseValue = baseEntity.getBaseValue(attribute);
        IBaseSet baseSet = save((IBaseSet)baseValue.getValue(), (IMetaSet)metaType, INITIAL_LEVEL);

        long baseValueId = save(baseEntity.getId(), metaAttribute.getId(), baseSet.getId(),
                baseValue.getBatch().getId(), baseValue.getIndex(),
                baseValue.getRepDate(), baseValue.isClosed(), baseValue.isLast());

        baseValue.setId(baseValueId);
        baseValue.setValue(baseSet);

        //return baseValue;
    }

    @Override
    public void save(final IBaseEntity baseEntity, Set<String> attributes) {
        for (String attribute: attributes)
        {
            save(baseEntity, attribute);
        }
    }

    protected IBaseSet save(IBaseSet baseSet, IMetaSet metaSet, long level)
    {
        IMetaType metaType = metaSet.getMemberType();
        if (metaType.isSet())
        {
            long parentBaseSetId = save(level, false);
            baseSet.setId(parentBaseSetId);

            for (IBaseValue childBaseValue : baseSet.get())
            {

                IBaseSet childBaseSet = save((IBaseSet) childBaseValue.getValue(), (IMetaSet)baseSet.getMemberType(), level + 1);

                long baseValueId = save(baseSet.getId(), childBaseSet.getId(),
                        childBaseValue.getBatch().getId(), childBaseValue.getIndex(), childBaseValue.getRepDate(),
                        childBaseValue.isClosed(), childBaseValue.isLast());

                childBaseValue.setId(baseValueId);
                childBaseValue.setValue(childBaseSet);
            }
        }
        else
        {
            long parentBaseSetId = save(level, true);
            baseSet.setId(parentBaseSetId);

            for (IBaseValue childBaseValue : baseSet.get())
            {

                Object value = null;
                if (metaType.isComplex())
                {
                    IBaseEntity baseEntity = baseEntityDao.saveOrUpdate((IBaseEntity)childBaseValue.getValue());
                    childBaseValue.setValue(baseEntity);

                    value = baseEntity.getId();
                }
                else
                {
                    value = childBaseValue.getValue();
                }

                long baseValueId = save(baseSet.getId(), childBaseValue.getBatch().getId(), childBaseValue.getIndex(),
                        childBaseValue.getRepDate(), value, childBaseValue.isClosed(), childBaseValue.isLast());

                childBaseValue.setId(baseValueId);
            }
        }

        return baseSet;
    }

    protected long save(long level, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_SETS)
                .set(EAV_BE_SETS.LEVEL, level)
                .set(EAV_BE_SETS.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    protected abstract long save(long baseEntityId, long metaAttributeId, long baseSetId, long batchId,
                                 long index, Date reportDate, boolean closed, boolean last);

    protected abstract long save(long parentSetId, long childSetId, long batchId, long index,
                                 Date reportDate, boolean closed, boolean last);

    protected abstract long save(long setId, long batchId, long index, Date reportDate,
                                 Object value, boolean closed, boolean last);


    @Override
    public void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void remove(final IBaseEntity baseEntity, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
