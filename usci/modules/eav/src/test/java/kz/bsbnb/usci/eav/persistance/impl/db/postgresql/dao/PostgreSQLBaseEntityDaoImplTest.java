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
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
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
            metaCreate.setMemberType("date_first", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("date_second", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("date_third", new MetaValue(DataTypes.DATE, false, true));

            //  double values
            metaCreate.setMemberType("double_first", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("double_second", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("double_third", new MetaValue(DataTypes.DOUBLE, false, true));

            // integer values
            metaCreate.setMemberType("integer_first", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("integer_second", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("integer_third", new MetaValue(DataTypes.INTEGER, false, true));

            // boolean values
            metaCreate.setMemberType("boolean_first", new MetaValue(DataTypes.BOOLEAN, false, true));
            metaCreate.setMemberType("boolean_second", new MetaValue(DataTypes.BOOLEAN, false, true));
            metaCreate.setMemberType("boolean_third", new MetaValue(DataTypes.BOOLEAN, false, true));

            // string values
            metaCreate.setMemberType("string_first", new MetaValue(DataTypes.STRING, false, true));
            metaCreate.setMemberType("string_second", new MetaValue(DataTypes.STRING, false, true));
            metaCreate.setMemberType("string_third", new MetaValue(DataTypes.STRING, false, true));

            // complex values
            metaCreate.setMemberType("complex_first", new MetaClassHolder("inner_meta_class_first"));
            metaCreate.setMemberType("complex_second", new MetaClassHolder("inner_meta_class_second"));
            metaCreate.setMemberType("complex_third", new MetaClassHolder("inner_meta_class_third"));

            // date array values
            metaCreate.setMemberType("date_array", new MetaValueArray(DataTypes.DATE));

            // double array values
            metaCreate.setMemberType("double_array", new MetaValueArray(DataTypes.DOUBLE));

            // integer array values
            metaCreate.setMemberType("integer_array", new MetaValueArray(DataTypes.INTEGER));

            // boolean array values
            metaCreate.setMemberType("boolean_array", new MetaValueArray(DataTypes.BOOLEAN));

            // string array values
            metaCreate.setMemberType("string_array", new MetaValueArray(DataTypes.STRING));

            // complex array values
            metaCreate.setMemberType("complex_array", new MetaClassArray(new MetaClassHolder("meta_class_array")));

            long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
            MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);


            Batch batch = batchRepository.addBatch(new Batch());
            BaseEntity entityCreate = new BaseEntity(metaLoad, batch);
            Random random = new Random();

            // date values
            entityCreate.set("date_first", 1L, DateUtils.nowPlus(Calendar.DATE, 1));
            entityCreate.set("date_second", 2L, DateUtils.nowPlus(Calendar.DATE, 2));
            entityCreate.set("date_third", 3L, null);

            // double values
            entityCreate.set("double_first", 4L, random.nextInt() * random.nextDouble());
            entityCreate.set("double_second", 5L, null);
            entityCreate.set("double_third", 6L, random.nextInt() * random.nextDouble());

            // integer values
            entityCreate.set("integer_first", 7L, null);
            entityCreate.set("integer_second", 8L, random.nextInt());
            entityCreate.set("integer_third", 9L, random.nextInt());

            // boolean values
            entityCreate.set("boolean_first", 10L, false);
            entityCreate.set("boolean_second", 11L, true);
            entityCreate.set("boolean_third", 12L, null);

            // string values
            entityCreate.set("string_first", 13L, "Test value with a string type for attribute string_first.");
            entityCreate.set("string_second", 14L, null);
            entityCreate.set("string_third", 15L, "Test value with a string type for attribute string_third.");

            // complex values
            BaseEntity baseEntityInnerFirst =
                    new BaseEntity(((MetaClassHolder)metaLoad.getMemberType("complex_first")).getMeta(), batch);
            entityCreate.set("complex_first", 16L, baseEntityInnerFirst);
            BaseEntity baseEntityInnerSecond =
                    new BaseEntity(((MetaClassHolder)metaLoad.getMemberType("complex_second")).getMeta(), batch);
            entityCreate.set("complex_second", 17L, baseEntityInnerSecond);

            // date array values
            entityCreate.addToArray("date_array", 18L, DateUtils.nowPlus(Calendar.DATE, 3));
            entityCreate.addToArray("date_array", 19L, DateUtils.nowPlus(Calendar.DATE, 5));
            entityCreate.addToArray("date_array", 20L, DateUtils.nowPlus(Calendar.DATE, 7));

            // double array values
            entityCreate.addToArray("double_array", 21L, random.nextInt() * random.nextDouble());
            entityCreate.addToArray("double_array", 22L, random.nextInt() * random.nextDouble());
            entityCreate.addToArray("double_array", 23L, random.nextInt() * random.nextDouble());

            // integer array values
            entityCreate.addToArray("integer_array", 24L, random.nextInt());
            entityCreate.addToArray("integer_array", 25L, random.nextInt());
            entityCreate.addToArray("integer_array", 26L, random.nextInt());

            // boolean array values
            entityCreate.addToArray("boolean_array", 27L, random.nextBoolean());
            entityCreate.addToArray("boolean_array", 28L, random.nextBoolean());
            entityCreate.addToArray("boolean_array", 29L, random.nextBoolean());

            // string array values
            entityCreate.addToArray("string_array", 30L, "First element of string array.");
            entityCreate.addToArray("string_array", 31L, "Second element of string array.");
            entityCreate.addToArray("string_array", 32L, "Third element of string array.");

            MetaClass metaClassForArrayElement = ((MetaClassArray)metaLoad.getMemberType("complex_array")).getMembersType().getMeta();
            BaseEntity baseEntityForArrayFirst = new BaseEntity(metaClassForArrayElement, batch);
            entityCreate.addToArray("complex_array", 33L, baseEntityForArrayFirst);
            BaseEntity baseEntityForArraySecond = new BaseEntity(metaClassForArrayElement, batch);
            entityCreate.addToArray("complex_array", 34L, baseEntityForArraySecond);

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
