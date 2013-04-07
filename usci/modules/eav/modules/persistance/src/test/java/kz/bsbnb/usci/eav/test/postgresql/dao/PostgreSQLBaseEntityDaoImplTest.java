/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.test.postgresql.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DateUtils;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.postgresql.dao.PostgreSQLBaseEntityDaoImpl;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.test.GenericTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.util.Calendar;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;


/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLBaseEntityDaoImplTest  extends GenericTestCase
{

    @Autowired
    IBatchRepository batchRepository;
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
        MetaClass metaCreate = new MetaClass("testMetaClass");

        // date values
        metaCreate.setMetaAttribute("date_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("date_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("date_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));

        //  double values
        metaCreate.setMetaAttribute("double_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));
        metaCreate.setMetaAttribute("double_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));
        metaCreate.setMetaAttribute("double_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));

        // integer values
        metaCreate.setMetaAttribute("integer_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.INTEGER)));
        metaCreate.setMetaAttribute("integer_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.INTEGER)));
        metaCreate.setMetaAttribute("integer_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.INTEGER)));

        // boolean values
        metaCreate.setMetaAttribute("boolean_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.BOOLEAN)));
        metaCreate.setMetaAttribute("boolean_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.BOOLEAN)));
        metaCreate.setMetaAttribute("boolean_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.BOOLEAN)));

        // string values
        metaCreate.setMetaAttribute("string_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaCreate.setMetaAttribute("string_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaCreate.setMetaAttribute("string_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));

        // complex values
        metaCreate.setMetaAttribute("complex_first",
                new MetaAttribute(false, true, new MetaClass("inner_meta_class_first")));
        metaCreate.setMetaAttribute("complex_second",
                new MetaAttribute(false, true, new MetaClass("inner_meta_class_second")));
        metaCreate.setMetaAttribute("complex_third",
                new MetaAttribute(false, true, new MetaClass("inner_meta_class_third")));

        // date array values
        metaCreate.setMetaAttribute("date_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.DATE))));

        // double array values
        /*metaCreate.setMemberType("double_array", new MetaValueArray(DataTypes.DOUBLE));

        // integer array values
        metaCreate.setMemberType("integer_array", new MetaValueArray(DataTypes.INTEGER));

        // boolean array values
        metaCreate.setMemberType("boolean_array", new MetaValueArray(DataTypes.BOOLEAN));

        // string array values
        metaCreate.setMemberType("string_array", new MetaValueArray(DataTypes.STRING));*/

        // complex array values
        metaCreate.setMetaAttribute("complex_set",
                new MetaAttribute(false, true, new MetaSet(new MetaClass("meta_class_set"))));

        metaCreate.setMetaAttribute("set_of_date_sets",
                new MetaAttribute(false, true, new MetaSet(new MetaSet(new MetaValue(DataTypes.DATE)))));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);


        Batch batch = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        BaseEntity entityCreate = new BaseEntity(metaLoad);
        Random random = new Random();

        // date values
        entityCreate.put("date_first",
                new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 1)));
        entityCreate.put("date_second",
                new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 2)));
        entityCreate.put("date_third",
                new BaseValue(batch, 1L, null));

        // double values
        entityCreate.put("double_first",
                new BaseValue(batch, 1L, random.nextInt() * random.nextDouble()));
        entityCreate.put("double_second",
                new BaseValue(batch, 1L, null));
        entityCreate.put("double_third",
                new BaseValue(batch, 1L, random.nextInt() * random.nextDouble()));

        // integer values
        entityCreate.put("integer_first",
                new BaseValue(batch, 1L, null));
        entityCreate.put("integer_second",
                new BaseValue(batch, 1L, random.nextInt()));
        entityCreate.put("integer_third",
                new BaseValue(batch, 1L, random.nextInt()));

        // boolean values
        entityCreate.put("boolean_first",
                new BaseValue(batch, 1L, false));
        entityCreate.put("boolean_second",
                new BaseValue(batch, 1L, true));
        entityCreate.put("boolean_third",
                new BaseValue(batch, 1L, null));

        // string values
        entityCreate.put("string_first",
                new BaseValue(batch, 1L, "Test value with a string type for attribute string_first."));
        entityCreate.put("string_second",
                new BaseValue(batch, 1L, null));
        entityCreate.put("string_third",
                new BaseValue(batch, 1L, "Test value with a string type for attribute string_third."));

        // complex values
        BaseEntity baseEntityInnerFirst = new BaseEntity((MetaClass)metaLoad.getMemberType("complex_first"));
        entityCreate.put("complex_first", new BaseValue(batch, 1L, baseEntityInnerFirst));
        BaseEntity baseEntityInnerSecond = new BaseEntity((MetaClass)metaLoad.getMemberType("complex_second"));
        entityCreate.put("complex_second", new BaseValue(batch, 1L, baseEntityInnerSecond));

        // date array values
        BaseSet baseSetForDate = new BaseSet(((MetaSet)metaLoad.getMemberType("date_set")).getMemberType());
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 3)));
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 5)));
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 7)));

        entityCreate.put("date_set", new BaseValue(batch, 1L, baseSetForDate));

        // double array values
        /*entityCreate.addToArray("double_array", 21L, random.nextInt() * random.nextDouble());
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
        entityCreate.addToArray("string_array", 32L, "Third element of string array."); */

        /*BaseSet baseSetForComplex = new BaseSet(((MetaSet)metaLoad.getMemberType("complex_set")).getMemberType());

        MetaClass metaClassForArrayElement = (MetaClass)((MetaSet)metaLoad.getMemberType("complex_set")).getMemberType();
        BaseEntity baseEntityForArrayFirst = new BaseEntity(metaClassForArrayElement);
        BaseEntity baseEntityForArraySecond = new BaseEntity(metaClassForArrayElement);

        baseSetForComplex.put(new BaseValue(batch, 1L, baseEntityForArrayFirst));
        baseSetForComplex.put(new BaseValue(batch, 1L, baseEntityForArraySecond));

        entityCreate.put("complex_set", new BaseValue(batch, 1L, baseSetForComplex)); */

        long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
        BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

        // simple values
        for (DataTypes dataType: DataTypes.values())
        {
            Set<String> attributeNamesCreate = entityCreate.getPresentSimpleAttributeNames(dataType);
            Set<String> attributeNamesLoad = entityLoad.getPresentSimpleAttributeNames(dataType);
            assertEquals(
                    String.format("One of the values with type %s are not saved or loaded.", dataType),
                    attributeNamesCreate.size(), attributeNamesLoad.size());

            for (String attributeName: attributeNamesCreate)
            {
                IBaseValue baseValueLoad = entityLoad.getBaseValue(attributeName);
                if (baseValueLoad != null)
                {
                    IBaseValue baseValueCreate = entityCreate.getBaseValue(attributeName);
                    assertEquals(
                            String.format(
                                    "Not properly saved or loaded an index field of one of the values with type %s.",
                                    dataType),
                            baseValueCreate.getIndex(), baseValueLoad.getIndex());

                    Batch batchCreate = baseValueCreate.getBatch();
                    Batch batchLoad = baseValueLoad.getBatch();

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
                            baseValueCreate.getIndex(), baseValueLoad.getIndex());

                    Object valueCreate = baseValueCreate.getValue();
                    Object valueLoad = baseValueLoad.getValue();

                    if (dataType.equals(DataTypes.DATE)) {
                        assertTrue(
                                String.format("Not properly saved or loaded value with type %s.", DataTypes.DATE),
                                (valueCreate == null && valueLoad == null) ||
                                        (valueCreate != null && valueLoad != null
                                                && (DateUtils.compareBeginningOfTheDay((java.util.Date)valueCreate, (java.util.Date)valueLoad) == 0)));
                    } else {
                        assertTrue(
                                String.format("Not properly saved or loaded value with type %s.", dataType),
                                (valueCreate == null && valueLoad == null) ||
                                        (valueCreate != null && valueLoad != null && valueCreate.equals(valueLoad)));
                    }
                }
            }
        }

        Set<String> attributeNamesCreate = entityCreate.getPresentComplexAttributeNames();
        Set<String> attributeNamesLoad = entityLoad.getPresentComplexAttributeNames();
        assertEquals(
                "One of the complex values are not saved or loaded.",
                attributeNamesCreate.size(), attributeNamesLoad.size());
    }

    @Test
    public void saveBaseValueWithSetOfDateSets() throws Exception {
        MetaClass metaCreate = new MetaClass("testMetaClass");

        metaCreate.setMetaAttribute("set_of_date_sets",
                new MetaAttribute(false, true, new MetaSet(new MetaSet(new MetaValue(DataTypes.DATE)))));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batch = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        BaseEntity entityCreate = new BaseEntity(metaLoad);

        MetaSet metaSetParent = (MetaSet)metaLoad.getMemberType("set_of_date_sets");
        MetaSet metaSetChild = (MetaSet)metaSetParent.getMemberType();

        BaseSet baseSetChildCreate = new BaseSet(metaSetChild);
        baseSetChildCreate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 10)));
        baseSetChildCreate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 20)));
        baseSetChildCreate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 30)));

        BaseSet baseSetParentCreate = new BaseSet(metaSetParent);
        baseSetParentCreate.put(new BaseValue(batch, 1L, baseSetChildCreate));

        entityCreate.put("set_of_date_sets", new BaseValue(batch, 1L, baseSetParentCreate));

        long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
        BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

        long countCreate = entityCreate.getAttributeCount();
        long countLoad = entityLoad.getAttributeCount();
        assertEquals(countCreate, countLoad);
    }

    @Test
    public void saveBaseValueWithDateSet() throws Exception {
        MetaClass metaCreate = new MetaClass("testMetaClass");
        metaCreate.setMetaAttribute("date_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.DATE))));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batch = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        BaseEntity entityCreate = new BaseEntity(metaLoad);

        Random random = new Random();

        BaseSet baseSetForDate = new BaseSet(((MetaSet)metaLoad.getMemberType("date_set")).getMemberType());
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 3)));
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 5)));
        baseSetForDate.put(new BaseValue(batch, 1L, DateUtils.nowPlus(Calendar.DATE, 7)));

        entityCreate.put("date_set", new BaseValue(batch, 1L, baseSetForDate));

        long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
        BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

        long countCreate = entityCreate.getAttributeCount();
        long countLoad = entityLoad.getAttributeCount();
        assertEquals(countCreate, countLoad);
    }

    @Test
    public void saveBaseValueWithComplexSet() throws Exception {
        MetaClass metaCreate = new MetaClass("testMetaClass");
        metaCreate.setMetaAttribute("complex_set",
                new MetaAttribute(false, true, new MetaSet(new MetaClass("meta_class_set"))));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batch = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        BaseEntity entityCreate = new BaseEntity(metaLoad);

        Random random = new Random();

        BaseSet baseSetForComplex = new BaseSet(((MetaSet)metaLoad.getMemberType("complex_set")).getMemberType());

        MetaClass metaClassForArrayElement = (MetaClass)((MetaSet)metaLoad.getMemberType("complex_set")).getMemberType();
        BaseEntity baseEntityForArrayFirst = new BaseEntity(metaClassForArrayElement);
        BaseEntity baseEntityForArraySecond = new BaseEntity(metaClassForArrayElement);

        baseSetForComplex.put(new BaseValue(batch, 1L, baseEntityForArrayFirst));
        baseSetForComplex.put(new BaseValue(batch, 1L, baseEntityForArraySecond));

        entityCreate.put("complex_set", new BaseValue(batch, 1L, baseSetForComplex));

        long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
        BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

        long countCreate = entityCreate.getAttributeCount();
        long countLoad = entityLoad.getAttributeCount();
        assertEquals(countCreate, countLoad);
    }

    @Test
    public void updateBaseEntityWithDateValues() throws Exception {
        MetaClass metaCreate = new MetaClass("testMetaClass");

        metaCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));

        metaCreate.setMetaAttribute("date_first",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("date_second",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("date_third",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("date_fourth",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batchFirst = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        Batch batchSecond = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));

        UUID uuid = UUID.randomUUID();

        BaseEntity entityForSave = new BaseEntity(metaLoad);
        entityForSave.put("uuid",
                new BaseValue(batchFirst, 1L, uuid.toString()));
        entityForSave.put("date_first",
                new BaseValue(batchFirst, 1L, DateUtils.nowPlus(Calendar.DATE, 1)));
        entityForSave.put("date_second",
                new BaseValue(batchFirst, 1L, DateUtils.nowPlus(Calendar.DATE, 2)));
        entityForSave.put("date_third",
                new BaseValue(batchFirst, 1L, DateUtils.nowPlus(Calendar.DATE, 3)));

        long entitySavedId = postgreSQLBaseEntityDaoImpl.save(entityForSave);
        BaseEntity entitySaved = postgreSQLBaseEntityDaoImpl.load(entitySavedId);

        BaseEntity entityForUpdate = new BaseEntity(metaLoad);
        entityForUpdate.put("uuid",
                new BaseValue(batchSecond, 2L, uuid.toString()));
        entityForUpdate.put("date_first",
                new BaseValue(batchSecond, 2L, null));
        entityForUpdate.put("date_second",
                new BaseValue(batchSecond, 2L, DateUtils.nowPlus(Calendar.DATE, 4)));
        entityForUpdate.put("date_fourth",
                new BaseValue(batchSecond, 2L, DateUtils.nowPlus(Calendar.DATE, 5)));

        long entityUpdatedId = postgreSQLBaseEntityDaoImpl.saveOrUpdate(entityForUpdate);
        BaseEntity entityUpdated = postgreSQLBaseEntityDaoImpl.load(entityUpdatedId);

        assertEquals("Incorrect number of attribute values in the saved BaseEntity,",
                4, entitySaved.getAttributeCount());
        assertEquals("Incorrect number of attribute values in the updated BaseEntity,",
                4, entityUpdated.getAttributeCount());

        Set<String> attributeNames = entityUpdated.getAttributeNames();

        assertFalse("Attribute value designed to remove is not removed.", attributeNames.contains("date_first"));
        assertTrue("Attribute value designed to insert is not inserted.", attributeNames.contains("date_fourth"));
        assertTrue("Attribute value designed to update is not updated.",
                DateUtils.compareBeginningOfTheDay(
                        (java.util.Date) entityUpdated.getBaseValue("date_second").getValue(),
                        DateUtils.nowPlus(Calendar.DATE, 4)) == 0);
    }

    @Test
    public void updateBaseEntityWithComplexValues() throws Exception {
        MetaClass metaCreate = new MetaClass("meta_class");

        metaCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));

        metaCreate.setMetaAttribute("inner_meta_class_first",
                new MetaAttribute(false, true, new MetaClass("meta_class_first")));
        metaCreate.setMetaAttribute("inner_meta_class_second",
                new MetaAttribute(false, true, new MetaClass("meta_class_second")));
        metaCreate.setMetaAttribute("inner_meta_class_third",
                new MetaAttribute(false, true, new MetaClass("meta_class_third")));
        metaCreate.setMetaAttribute("inner_meta_class_fourth",
                new MetaAttribute(false, true, new MetaClass("meta_class_fourth")));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batchFirst = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        Batch batchSecond = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));

        UUID uuid = UUID.randomUUID();

        BaseEntity entityForSave = new BaseEntity(metaLoad);
        BaseEntity entityFirstForSave =
                new BaseEntity((MetaClass)metaLoad.getMemberType("inner_meta_class_first"));
        BaseEntity entitySecondForSave =
                new BaseEntity((MetaClass)metaLoad.getMemberType("inner_meta_class_second"));
        BaseEntity entityThirdForSave =
                new BaseEntity((MetaClass)metaLoad.getMemberType("inner_meta_class_third"));

        entityForSave.put("uuid",
                new BaseValue(batchFirst, 1L, uuid.toString()));
        entityForSave.put("inner_meta_class_first",
                new BaseValue(batchFirst, 1L, entityFirstForSave));
        entityForSave.put("inner_meta_class_second",
                new BaseValue(batchFirst, 1L, entitySecondForSave));
        entityForSave.put("inner_meta_class_third",
                new BaseValue(batchFirst, 1L, entityThirdForSave));

        long entitySavedId = postgreSQLBaseEntityDaoImpl.save(entityForSave);
        BaseEntity entitySaved = postgreSQLBaseEntityDaoImpl.load(entitySavedId);

        BaseEntity entityForUpdate = new BaseEntity(metaLoad);
        BaseEntity entitySecondForUpdate =
                new BaseEntity((MetaClass)metaLoad.getMemberType("inner_meta_class_second"));
        BaseEntity entityFourthForUpdate =
                new BaseEntity((MetaClass)metaLoad.getMemberType("inner_meta_class_second"));
        entityForUpdate.put("uuid",
                new BaseValue(batchSecond, 2L, uuid.toString()));
        entityForUpdate.put("inner_meta_class_first",
                new BaseValue(batchSecond, 2L, null));
        entityForUpdate.put("inner_meta_class_second",
                new BaseValue(batchSecond, 2L, entitySecondForUpdate));
        entityForUpdate.put("inner_meta_class_fourth",
                new BaseValue(batchSecond, 2L, entityFourthForUpdate));

        long entityUpdatedId = postgreSQLBaseEntityDaoImpl.saveOrUpdate(entityForUpdate);
        BaseEntity entityUpdated = postgreSQLBaseEntityDaoImpl.load(entityUpdatedId);

        assertEquals("Incorrect number of attribute values in the saved BaseEntity,",
                4, entitySaved.getAttributeCount());
        assertEquals("Incorrect number of attribute values in the updated BaseEntity,",
                4, entityUpdated.getAttributeCount());

        Set<String> attributeNames = entityUpdated.getAttributeNames();

        assertFalse("Attribute value designed to remove is not removed.",
                attributeNames.contains("inner_meta_class_first"));
        assertTrue("Attribute value designed to insert is not inserted.",
                attributeNames.contains("inner_meta_class_fourth"));

        IBaseValue baseValueFirstUpdated = entityUpdated.getBaseValue("inner_meta_class_first");
        assertNull("The upgrade process was not deleted attribute.", baseValueFirstUpdated);

        IBaseValue baseValueSecond = entityUpdated.getBaseValue("inner_meta_class_second");
        assertNotNull("The upgrade process has been removed or not loaded attribute.", baseValueSecond);
        /*assertEquals("During the saveOrUpdate field INDEX was not changed,",
                2L, baseValueSecond.getIndex());*/
        /*assertEquals("During the saveOrUpdate field BATCH_ID was not changed,",
                batchSecond.getId(), baseValueSecond.getBatch().getId());*/

        IBaseValue baseValueThird = entityUpdated.getBaseValue("inner_meta_class_third");
        assertNotNull("The upgrade process has been removed or not loaded attribute.", baseValueThird);
        assertEquals("During the update field INDEX was changed,",
                1L, baseValueThird.getIndex());
        assertEquals("During the update field BATCH_ID was changed,",
                batchFirst.getId(), baseValueThird.getBatch().getId());
    }

    @Test
    public void updateBaseEntityWithComplexSetValuesFirst() throws Exception {
        MetaClass metaParentCreate = new MetaClass("meta_class_parent");
        MetaClass metaChildCreate = new MetaClass("meta_class_child");
        metaParentCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaParentCreate.setMetaAttribute("complex_set",
                new MetaAttribute(false, true, new MetaSet(metaChildCreate)));
        assertTrue("Searchable valueSetUpdated must be equal to true.", metaParentCreate.isSearchable());
        assertFalse("Searchable valueSetUpdated must be equal to false.", metaChildCreate.isSearchable());

        long metaId = postgreSQLMetaClassDaoImpl.save(metaParentCreate);
        MetaClass metaParentLoad = postgreSQLMetaClassDaoImpl.load(metaId);
        MetaClass metaChildLoad =
                (MetaClass)((MetaSet)metaParentLoad.getMemberType("complex_set")).getMemberType();
        assertTrue("Searchable valueSetUpdated must be equal to true.", metaParentLoad.isSearchable());
        assertFalse("Searchable valueSetUpdated must be equal to false.", metaChildLoad.isSearchable());

        Batch batchFirst = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));
        Batch batchSecond = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));

        UUID uuid = UUID.randomUUID();

        BaseEntity entityParentForSave = new BaseEntity(metaParentLoad);
        BaseSet baseSetForSave = new BaseSet(metaParentLoad.getMemberType("complex_set"));
        baseSetForSave.put(new BaseValue(batchFirst, 1L, new BaseEntity(metaChildLoad)));
        entityParentForSave.put("uuid", new BaseValue(batchFirst, 1L, uuid.toString()));
        entityParentForSave.put("complex_set", new BaseValue(batchFirst, 1L, baseSetForSave));

        long entityParentSavedId = postgreSQLBaseEntityDaoImpl.save(entityParentForSave);
        BaseEntity entityParentSaved = postgreSQLBaseEntityDaoImpl.load(entityParentSavedId);

        BaseEntity entityParentForUpdate = new BaseEntity(metaParentLoad);
        BaseSet baseSetForUpdate = new BaseSet(metaParentLoad.getMemberType("complex_set"));
        baseSetForUpdate.put(new BaseValue(batchSecond, 2L, new BaseEntity(metaChildLoad)));
        entityParentForUpdate.put("uuid", new BaseValue(batchSecond, 2L, uuid.toString()));
        entityParentForUpdate.put("complex_set", new BaseValue(batchSecond, 2L, baseSetForUpdate));

        long entityParentUpdatedId = postgreSQLBaseEntityDaoImpl.saveOrUpdate(entityParentForUpdate);
        BaseEntity entityParentUpdated = postgreSQLBaseEntityDaoImpl.load(entityParentUpdatedId);

        assertEquals("Incorrect number of attribute values in the saved BaseEntity,",
                2, entityParentSaved.getAttributeCount());
        assertEquals("Incorrect number of attribute values in the updated BaseEntity,",
                2, entityParentUpdated.getAttributeCount());

        IBaseValue valueSetSaved = entityParentSaved.getBaseValue("complex_set");
        IBaseValue valueSetUpdated = entityParentUpdated.getBaseValue("complex_set");
        assertNotNull("BaseValue instance contains an instance of BaseSet " +
                "after saving was equal to null.", valueSetSaved);
        assertNotNull("BaseValue instance contains an instance of BaseSet " +
                "after updating was equal to null.", valueSetUpdated);
        assertTrue("Identifiers of BaseValue instances containing instances of BaseSet " +
                "before and after updating the set of complex values should be different,",
                valueSetSaved.getId() != valueSetUpdated.getId());

        BaseSet setSaved = (BaseSet)valueSetSaved.getValue();
        BaseSet setUpdated = (BaseSet)valueSetUpdated.getValue();
        assertNotNull("After saving instance of BaseSet was equal to null.", setSaved);
        assertNotNull("After updating instance of BaseSet was equal to null.", setUpdated);

        Set<IBaseValue> setValuesSaved = setSaved.get();
        Set<IBaseValue> setValuesUpdated = setUpdated.get();
        assertEquals("Wrong number of elements in the set of complex values after saving, ",
                1, setValuesSaved.size());
        assertEquals("Wrong number of elements in the set of complex values after updating, ",
                1, setValuesUpdated.size());

        BaseValue valueEntityChildSaved = (BaseValue)setValuesSaved.toArray()[0];
        BaseValue valueEntityChildUpdated = (BaseValue)setValuesUpdated.toArray()[0];
        assertNotNull("BaseValue instance contains an instance of BaseEntity " +
                "after saving was equal null.", valueEntityChildSaved);
        assertNotNull("BaseValue instance contains an instance of BaseEntity " +
                "after updating was equal null.", valueEntityChildUpdated);

        BaseEntity entityChildSaved = (BaseEntity)valueEntityChildSaved.getValue();
        BaseEntity entityChildUpdated = (BaseEntity)valueEntityChildUpdated.getValue();
        assertNotNull("After saving instance of BaseEntity was equal to null.", entityChildSaved);
        assertNotNull("After updating instance of BaseEntity was equal to null.", entityChildUpdated);
        assertTrue("BaseEntity identifiers before and after updating the set of complex values should be different, ",
                entityChildSaved.getId() != entityChildUpdated.getId());

        try
        {
            BaseEntity entityChildSearched = postgreSQLBaseEntityDaoImpl.load(entityChildSaved.getId());
            fail("Expected an illegal state exception.");
        }
        catch(IllegalStateException e)
        {
            // Nothing
        }
    }

    @Test
    public void  searchBaseEntity() throws Exception {
        MetaClass metaCreate = new MetaClass("meta_class");
        metaCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));

        long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
        MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

        Batch batch = batchRepository.addBatch(new Batch(new Date(System.currentTimeMillis())));

        UUID uuid = UUID.randomUUID();

        BaseEntity entityForSave = new BaseEntity(metaLoad);
        entityForSave.put("uuid",
                new BaseValue(batch, 1L, uuid.toString()));

        long entitySavedId = postgreSQLBaseEntityDaoImpl.save(entityForSave);
        BaseEntity entitySaved = postgreSQLBaseEntityDaoImpl.load(entitySavedId);

        BaseEntity entityForSearch = new BaseEntity(metaLoad);
        entityForSearch.put("uuid",
                new BaseValue(batch, 1L, uuid.toString()));

        BaseEntity entitySearched = postgreSQLBaseEntityDaoImpl.search(entityForSearch);
        assertNotNull("Search engine was not found necessary BaseEntity.", entitySearched);
        assertEquals("Search engine was found BaseEntity with incorrect id,",
                entitySearched.getId(), entitySaved.getId());
    }



}
