package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep5;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DOUBLE_SET_VALUES;

/**
 *
 */
@Repository
public class BeSimpleSetValueDaoImpl extends JDBCSupport implements IBeSimpleSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @Autowired
    IBeSetValueDao beSetValueDao;

    public long save(IBaseValue baseValue, MetaSet metaSet)
    {
        IMetaType metaType = metaSet.getMemberType();
        BaseSet baseSet = (BaseSet)baseValue.getValue();

        long setId = beSetValueDao.save(baseValue);
        if (metaType.isSet())
        {
            InsertValuesStep2 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_SET_OF_SIMPLE_SETS,
                            EAV_BE_SET_OF_SIMPLE_SETS.PARENT_SET_ID,
                            EAV_BE_SET_OF_SIMPLE_SETS.CHILD_SET_ID);

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();

            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                MetaSet baseSetChild = (MetaSet)metaType;

                long setIdChild = save(baseValueChild, baseSetChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        setIdChild,
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }
        else
        {
            InsertValuesStep5 insert;
            DataTypes dataType = metaSet.getTypeCode();
            switch(dataType)
            {
                case INTEGER:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_INTEGER_SET_VALUES,
                                    EAV_BE_INTEGER_SET_VALUES.SET_ID,
                                    EAV_BE_INTEGER_SET_VALUES.BATCH_ID,
                                    EAV_BE_INTEGER_SET_VALUES.INDEX_,
                                    EAV_BE_INTEGER_SET_VALUES.REP_DATE,
                                    EAV_BE_INTEGER_SET_VALUES.VALUE);
                    break;
                }
                case DATE:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_DATE_SET_VALUES,
                                    EAV_BE_DATE_SET_VALUES.SET_ID,
                                    EAV_BE_DATE_SET_VALUES.BATCH_ID,
                                    EAV_BE_DATE_SET_VALUES.INDEX_,
                                    EAV_BE_DATE_SET_VALUES.REP_DATE,
                                    EAV_BE_DATE_SET_VALUES.VALUE);
                    break;
                }
                case STRING:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_STRING_SET_VALUES,
                                    EAV_BE_STRING_SET_VALUES.SET_ID,
                                    EAV_BE_STRING_SET_VALUES.BATCH_ID,
                                    EAV_BE_STRING_SET_VALUES.INDEX_,
                                    EAV_BE_STRING_SET_VALUES.REP_DATE,
                                    EAV_BE_STRING_SET_VALUES.VALUE);
                    break;
                }
                case BOOLEAN:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_BOOLEAN_SET_VALUES,
                                    EAV_BE_BOOLEAN_SET_VALUES.SET_ID,
                                    EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID,
                                    EAV_BE_BOOLEAN_SET_VALUES.INDEX_,
                                    EAV_BE_BOOLEAN_SET_VALUES.REP_DATE,
                                    EAV_BE_BOOLEAN_SET_VALUES.VALUE);
                    break;
                }
                case DOUBLE:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_DOUBLE_SET_VALUES,
                                    EAV_BE_DOUBLE_SET_VALUES.SET_ID,
                                    EAV_BE_DOUBLE_SET_VALUES.BATCH_ID,
                                    EAV_BE_DOUBLE_SET_VALUES.INDEX_,
                                    EAV_BE_DOUBLE_SET_VALUES.REP_DATE,
                                    EAV_BE_DOUBLE_SET_VALUES.VALUE);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue batchValueChild = it.next();
                Object[] insertArgs = new Object[] {
                        setId,
                        batchValueChild.getBatch().getId(),
                        batchValueChild.getIndex(),
                        batchValueChild.getRepDate(),
                        batchValueChild.getValue()
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }

        return setId;
    }

    @Override
    public void remove(BaseSet baseSet) {
        long setId = baseSet.getId();
        IMetaType metaType = baseSet.getMemberType();
        MetaSet metaSet = (MetaSet)metaType;
        if (metaType.isSet())
        {
            DeleteConditionStep delete = sqlGenerator
                    .delete(EAV_BE_SET_OF_SIMPLE_SETS)
                    .where(EAV_BE_SET_OF_SIMPLE_SETS.PARENT_SET_ID.eq(setId));

            logger.debug(delete.toString());
            updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

            beSetValueDao.remove(baseSet);

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                remove((BaseSet)baseValueChild.getValue());
            }
        }
        else
        {
            DeleteConditionStep delete;
            DataTypes dataType = metaSet.getTypeCode();
            switch(dataType)
            {
                case INTEGER:
                {
                    delete = sqlGenerator
                            .delete(EAV_BE_INTEGER_SET_VALUES)
                            .where(EAV_BE_INTEGER_SET_VALUES.SET_ID.eq(setId));
                    break;
                }
                case DATE:
                {
                    delete = sqlGenerator
                            .delete(EAV_BE_DATE_SET_VALUES)
                            .where(EAV_BE_DATE_SET_VALUES.SET_ID.eq(setId));
                    break;
                }
                case STRING:
                {
                    delete = sqlGenerator
                            .delete(EAV_BE_STRING_SET_VALUES)
                            .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId));
                    break;
                }
                case BOOLEAN:
                {
                    delete = sqlGenerator
                            .delete(EAV_BE_BOOLEAN_SET_VALUES)
                            .where(EAV_BE_BOOLEAN_SET_VALUES.SET_ID.eq(setId));
                    break;
                }
                case DOUBLE:
                {
                    delete = sqlGenerator
                            .delete(EAV_BE_DOUBLE_SET_VALUES)
                            .where(EAV_BE_DOUBLE_SET_VALUES.SET_ID.eq(setId));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }
            logger.debug(delete.toString());
            batchUpdateWithStats(delete.getSQL(), delete.getBindValues());

            beSetValueDao.remove(baseSet);
        }
    }

}
