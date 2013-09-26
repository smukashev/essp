package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class AbstractBeValueDaoImpl extends JDBCSupport implements IBeValueDao {

    private final Logger logger = LoggerFactory.getLogger(AbstractBeValueDaoImpl.class);

    @Autowired
    private IBaseEntityDao baseEntityDao;

    @Override
    public IBaseValue save(IBaseEntity baseEntity, String attribute) {
        IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        long baseValueId = save(baseEntity.getId(), baseValue.getBatch().getId(), metaAttribute.getId(), baseValue.getIndex(),
                baseValue.getRepDate(), baseValue.getValue(), baseValue.isClosed(), baseValue.isLast());
        baseValue.setId(baseValueId);

        return baseValue;
    }

    @Override
    public Map<String, IBaseValue> save(IBaseEntity baseEntity, Set<String> attributes) {
        Map<String, IBaseValue> values = new HashMap<String, IBaseValue>();
        for (String attribute: attributes)
        {
            IBaseValue baseValue = save(baseEntity, attribute);
            values.put(attribute, baseValue);
        }
        return values;
    }

    @Override
    public IBaseValue update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute) {
        IMetaAttribute metaAttribute = baseEntityLoaded.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
        IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

        Object value = null;
        if (baseValueForSave.getValue() != null)
        {
            if (metaType.isComplex())
            {
                IBaseEntity baseEntity = baseEntityDao.saveOrUpdate((IBaseEntity)baseValueForSave.getValue());
                baseValueForSave.setValue(baseEntity);

                value = baseEntity.getId();
            }
            else
            {
                value = baseValueForSave.getValue();
            }
        }

        if (baseValueLoaded == null)
        {
            if (baseValueForSave == null)
            {
                logger.warn(String.format("An attempt was made to remove a missing value for the " +
                        "attribute {0} of BaseEntity instance with identifier {1}.", baseEntityLoaded.getId(), attribute));
            }
            else
            {
                long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                        metaAttribute.getId(), baseValueForSave.getIndex(),
                        baseValueForSave.getRepDate(), value, false, true);
                baseValueForSave.setId(baseValueId);
            }
        }
        else
        {
            int compare = DataUtils.compareBeginningOfTheDay(baseValueForSave.getRepDate(), baseValueLoaded.getRepDate());
            switch(compare)
            {
                case 1:
                {
                    long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(), baseValueForSave.getRepDate(),
                            value, baseValueForSave.isClosed(), baseValueForSave.isLast());
                    baseValueForSave.setId(baseValueId);

                    if (baseValueLoaded.isLast())
                    {
                        Map<String, Object> fields = new HashMap<String, Object>();
                        fields.put("is_last", false);

                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("id", baseValueLoaded.getId());

                        updateByCondition(fields, conditions);
                    }
                    break;
                }
                case 0:
                    Map<String, Object> fields = new HashMap<String, Object>();
                    fields.put("batch_id", baseValueForSave.getBatch().getId());
                    fields.put("index_", baseValueForSave.getIndex());
                    fields.put("value", value);
                    if (baseValueForSave.isClosed())
                    {
                        fields.put("is_closed", true);
                    }

                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("id", baseValueLoaded.getId());

                    updateByCondition(fields, conditions);

                    break;
                case -1:
                {
                    long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(),
                            baseValueForSave.getRepDate(), value, baseValueForSave.isClosed(), false);
                    baseValueForSave.setId(baseValueId);
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
    public boolean presentInFuture(IBaseEntity baseEntity, String attribute)
    {
        IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
        return presentInFuture(baseEntity.getId(), metaAttribute.getId(), baseEntity.getReportDate());
    }

    @Override
    public void remove(IBaseEntity baseEntity, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    protected abstract long save(long baseEntityId, long batchId, long metaAttributeId, long index,
                                 Date reportDate, Object value, boolean closed, boolean last);

    protected abstract int updateByCondition(Map<String, Object> fields,
                                             Map<String, Object> conditions);

    protected abstract boolean presentInFuture(long entityId, long attributeId, Date reportDate);

}

