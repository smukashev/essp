/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassHolder;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLBaseEntityDaoImplTest {

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IStorage postgreSQLStorageImpl;
    @Autowired
    IMetaClassDao postgreSQLMetaClassDaoImpl;
    @Autowired
    IBaseEntityDao postgreSQLBaseEntityDaoImpl;
    @Autowired
    IBatchDao postgreSQLBatchEntityDaoImpl;

    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    public PostgreSQLBaseEntityDaoImplTest() {
    }

    @Before
    public void initialization() throws Exception {

    }

    @After
    public void finalization() throws Exception {

    }

    @Test
    public void saveBaseEntity() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            MetaClass metaCreate = new MetaClass("testMetaClass");

            // date values
            metaCreate.setMemberType("testDate1", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("testDate2", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("testDate3", new MetaValue(DataTypes.DATE, false, true));

            //  double values
            metaCreate.setMemberType("testDouble1", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("testDouble2", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("testDouble3", new MetaValue(DataTypes.DOUBLE, false, true));

            // integer values
            metaCreate.setMemberType("testInteger1", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("testInteger2", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("testInteger3", new MetaValue(DataTypes.INTEGER, false, true));

            // boolean values
            metaCreate.setMemberType("testBoolean1", new MetaValue(DataTypes.BOOLEAN, false, true));
            metaCreate.setMemberType("testBoolean2", new MetaValue(DataTypes.BOOLEAN, false, true));
            metaCreate.setMemberType("testBoolean3", new MetaValue(DataTypes.BOOLEAN, false, true));

            // string values
            metaCreate.setMemberType("testString1", new MetaValue(DataTypes.STRING, false, true));
            metaCreate.setMemberType("testString2", new MetaValue(DataTypes.STRING, false, true));
            metaCreate.setMemberType("testString3", new MetaValue(DataTypes.STRING, false, true));

            // complex values
            metaCreate.setMemberType("testComplex1", new MetaClassHolder("testMetaClass1"));
            metaCreate.setMemberType("testComplex2", new MetaClassHolder("testMetaClass2"));
            metaCreate.setMemberType("testComplex3", new MetaClassHolder("testMetaClass3"));

            long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
            MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);


            Batch batch = batchRepository.addBatch(new Batch());
            BaseEntity entityCreate = new BaseEntity(metaLoad, batch);
            Random random = new Random();

            // date values
            entityCreate.set("testDate1", 1L, DateUtils.nowPlus(Calendar.DATE, 1));
            entityCreate.set("testDate2", 2L, DateUtils.nowPlus(Calendar.DATE, 2));
            entityCreate.set("testDate3", 3L, null);

            // double values
            entityCreate.set("testDouble1", 4L, 100000 * random.nextDouble());
            entityCreate.set("testDouble2", 5L, null);
            entityCreate.set("testDouble3", 6L, 100000 * random.nextDouble());

            // integer values
            entityCreate.set("testInteger1", 7L, null);
            entityCreate.set("testInteger2", 8L, random.nextInt());
            entityCreate.set("testInteger3", 9L, random.nextInt());

            // boolean values
            entityCreate.set("testBoolean1", 10L, false);
            entityCreate.set("testBoolean2", 11L, true);
            entityCreate.set("testBoolean3", 12L, null);

            // string values
            entityCreate.set("testString1", 13L, "Test value with a string type.");
            entityCreate.set("testString2", 14L, null);
            entityCreate.set("testString3", 15L, "Test value with a string type.");

            // complex values
            BaseEntity childBaseEntity1 =
                    new BaseEntity(((MetaClassHolder)metaLoad.getMemberType("testComplex1")).getMeta(), batch);
            entityCreate.set("testComplex1", 16L, childBaseEntity1);
            BaseEntity childBaseEntity2 =
                    new BaseEntity(((MetaClassHolder)metaLoad.getMemberType("testComplex2")).getMeta(), batch);
            entityCreate.set("testComplex2", 17L, childBaseEntity2);

            long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
            BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

            // simple values
            for (DataTypes dataType: DataTypes.values()) {
                Set<String> attributeNamesCreate = entityCreate.getPresentSimpleAttributeNames(dataType);
                Set<String> attributeNamesLoad = entityLoad.getPresentSimpleAttributeNames(dataType);
                assertEquals(
                        String.format("One of the values with type %s are not saved or loaded.", dataType),
                        attributeNamesCreate.size(), attributeNamesLoad.size());

                for (String attributeName: attributeNamesCreate)
                {
                    IBatchValue batchValueLoad = entityLoad.getBatchValue(attributeName);
                    if (batchValueLoad != null)
                    {
                        IBatchValue batchValueCreate = entityCreate.getBatchValue(attributeName);
                        assertEquals(
                                String.format(
                                        "Not properly saved or loaded an index field of one of the values with type %s.",
                                        dataType),
                                batchValueCreate.getIndex(), batchValueLoad.getIndex());

                        Batch batchCreate = batchValueCreate.getBatch();
                        Batch batchLoad = batchValueLoad.getBatch();

                        assertNotNull(
                                String.format(
                                        "When loading a simple attribute value to the type %s the Batch was equal to null",
                                        dataType),
                                batchLoad
                        );

                        assertEquals(
                                String.format(
                                        "Not properly saved or loaded an batch field of one of the values with type %s.",
                                        dataType),
                                batchCreate, batchLoad);

                        assertEquals(
                                String.format(
                                        "Not properly saved or loaded an index field of one of the values with type %s.",
                                        dataType),
                                batchValueCreate.getIndex(), batchValueLoad.getIndex());

                        Object valueCreate = batchValueCreate.getValue();
                        Object valueLoad = batchValueLoad.getValue();

                        if (dataType.equals(DataTypes.DATE)) {
                            assertTrue(
                                    String.format("Not properly saved or loaded value with type %s.", DataTypes.DATE),
                                    (valueCreate == null && valueLoad == null) ||
                                            (valueCreate != null && valueLoad != null
                                                    && (DateUtils.compareBeginningOfTheDay((Date)valueCreate, (Date)valueLoad) == 0)));
                        } else {
                            assertTrue(
                                    String.format("Not properly saved or loaded value with type %s.", dataType),
                                    (valueCreate == null && valueLoad == null) ||
                                            (valueCreate != null && valueLoad != null && valueCreate.equals(valueLoad)));
                        }
                    }
                }
            }
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void multipleBatchSave() throws Exception {

    }

}
