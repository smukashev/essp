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
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_SETS;

/**
 *
 */
public abstract class AbstractBeSetValueDaoImpl extends JDBCSupport implements IBeSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(AbstractBeSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBaseEntityDao baseEntityDao;

    public static final long INITIAL_LEVEL = 1;

    @Override
    public IBaseValue save(final IBaseEntity baseEntity, String attribute) {
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

        return baseValue;
    }

    @Override
    public Map<String, IBaseValue> save(final IBaseEntity baseEntity, Set<String> attributes) {
        Map<String, IBaseValue> values = new HashMap<String, IBaseValue>();
        for (String attribute: attributes)
        {
            IBaseValue baseValue = save(baseEntity, attribute);
            values.put(attribute, baseValue);
        }
        return values;
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

                Object value;
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
                .set(EAV_BE_SETS.LEVEL_, level)
                .set(EAV_BE_SETS.IS_LAST, DataUtils.convert(last));

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
    public IBaseValue update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute)
    {
        IMetaAttribute metaAttribute = baseEntityForSave.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        if (metaType.isSetOfSets())
        {
            throw new UnsupportedOperationException("Not yet implemented.");
        }

        IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
        IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

        if (baseValueLoaded == null)
        {
            if (baseValueForSave == null)
            {
                logger.warn(String.format("An attempt was made to remove a missing value for the " +
                        "attribute {0} of BaseEntity instance with identifier {1}.", baseEntityLoaded.getId(), attribute));
            }
            else
            {
                //TODO: Check this attribute in the future, if exist then isLast = false
                IBaseSet baseSet = save((IBaseSet)baseValueForSave.getValue(), (IMetaSet)metaType, INITIAL_LEVEL);

                long baseValueId = save(baseEntityForSave.getId(), metaAttribute.getId(), baseSet.getId(),
                        baseValueForSave.getBatch().getId(), baseValueForSave.getIndex(),
                        baseValueForSave.getRepDate(), false, true);

                baseValueForSave.setId(baseValueId);
                baseValueForSave.setValue(baseSet);
            }
        }
        else
        {


            int compare = DataUtils.compareBeginningOfTheDay(baseValueForSave.getRepDate(), baseValueLoaded.getRepDate());
            switch(compare)
            {
                case 1:
                {
                    /*long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(), baseValueForSave.getRepDate(),
                            baseValueForSave.getValue(), baseValueForSave.isClosed(), baseValueForSave.isLast());
                    baseValueForSave.setId(baseValueId);

                    if (baseValueLoaded.isLast())
                    {
                        Map<String, Object> fields = new HashMap<String, Object>();
                        fields.put("is_last", false);

                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("id", baseValueLoaded.getId());

                        updateByCondition(dataType, fields, conditions);
                    }*/
                    break;
                }
                case 0:
                    /*Map<String, Object> fields = new HashMap<String, Object>();
                    fields.put("batch_id", baseValueForSave.getBatch().getId());
                    fields.put("index_", baseValueForSave.getIndex());
                    fields.put("value", baseValueForSave.getValue());
                    if (baseValueForSave.isClosed())
                    {
                        fields.put("is_closed", true);
                    }

                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("id", baseValueLoaded.getId());

                    updateByCondition(dataType, fields, conditions);*/

                    break;
                case -1:
                {
                    /*long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(),
                            baseValueForSave.getRepDate(), baseValueForSave.getValue(), baseValueForSave.isClosed(), false);
                    baseValueForSave.setId(baseValueId);*/
                    break;
                }
                default:
                    throw new RuntimeException("Method Comparable<T>.compareTo(T o) " +
                            "can not return a value other than -1, 0, 1.");
            }
        }

        return baseValueForSave;
    }

    @Override
    public void remove(final IBaseEntity baseEntity, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
