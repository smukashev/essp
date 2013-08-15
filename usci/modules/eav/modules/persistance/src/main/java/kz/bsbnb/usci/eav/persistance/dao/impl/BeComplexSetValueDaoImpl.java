package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetValueDao;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 *
 */
@Repository
public class BeComplexSetValueDaoImpl extends JDBCSupport implements IBeComplexSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeComplexSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @Autowired
    IBaseEntityDao baseEntityDao;
    @Autowired
    IBeSetValueDao beSetValueDao;

    public long save(IBaseValue baseValue, MetaSet metaSet)
    {
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaType = metaSet.getMemberType();

        long setId = beSetValueDao.save(baseValue);
        if (metaType.isSet())
        {
            InsertValuesStep2 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_SET_OF_COMPLEX_SETS,
                            EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID,
                            EAV_BE_SET_OF_COMPLEX_SETS.CHILD_SET_ID);

            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();

            while (it.hasNext())
            {
                IBaseValue baseValueChild = it.next();
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
            InsertValuesStep5 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_COMPLEX_SET_VALUES,
                            EAV_BE_COMPLEX_SET_VALUES.SET_ID,
                            EAV_BE_COMPLEX_SET_VALUES.BATCH_ID,
                            EAV_BE_COMPLEX_SET_VALUES.INDEX_,
                            EAV_BE_COMPLEX_SET_VALUES.REP_DATE,
                            EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID);

            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext()) {
                IBaseValue baseValueChild = itValue.next();

                BaseEntity baseEntityChild = (BaseEntity)baseValueChild.getValue();
                long baseEntityChildId = baseEntityDao.saveOrUpdate(baseEntityChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        baseValueChild.getBatch().getId(),
                        baseValueChild.getIndex(),
                        baseValue.getRepDate(),
                        baseEntityChildId
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
        if (metaType.isSet())
        {
            DeleteConditionStep delete = sqlGenerator
                    .delete(EAV_BE_SET_OF_COMPLEX_SETS)
                    .where(EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID.eq(setId));

            logger.debug(delete.toString());
            updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

            beSetValueDao.remove(baseSet);

            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                remove((BaseSet) baseValueChild.getValue());
            }
        }
        else
        {
            DeleteConditionStep delete = sqlGenerator
                    .delete(EAV_BE_COMPLEX_SET_VALUES)
                    .where(EAV_BE_COMPLEX_SET_VALUES.SET_ID.eq(setId));

            logger.debug(delete.toString());
            batchUpdateWithStats(delete.getSQL(), delete.getBindValues());

            beSetValueDao.remove(baseSet);
        }
    }

}
