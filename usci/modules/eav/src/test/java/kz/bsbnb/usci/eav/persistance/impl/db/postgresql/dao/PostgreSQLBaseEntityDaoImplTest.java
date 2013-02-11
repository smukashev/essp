/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
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

            MetaClass metaCreate = new MetaClass();
            metaCreate.setClassName("testMetaClass");
            metaCreate.setMemberType("testDate1", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("testDate2", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("testDate3", new MetaValue(DataTypes.DATE, false, true));
            metaCreate.setMemberType("testDouble1", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("testDouble2", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("testDouble3", new MetaValue(DataTypes.DOUBLE, false, true));
            metaCreate.setMemberType("testInteger1", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("testInteger2", new MetaValue(DataTypes.INTEGER, false, true));
            metaCreate.setMemberType("testInteger3", new MetaValue(DataTypes.INTEGER, false, true));

            long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
            MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

            Batch batchCreate = new Batch();
            long batchId = postgreSQLBatchEntityDaoImpl.save(batchCreate);
            Batch batchLoad = postgreSQLBatchEntityDaoImpl.load(batchId);

            Random random = new Random();

            BaseEntity entityCreate = new BaseEntity(metaLoad, batchLoad);
            entityCreate.set("testDate1", 1L, DateUtils.nowPlus(Calendar.DATE, 1));
            entityCreate.set("testDate2", 2L, DateUtils.nowPlus(Calendar.DATE, 2));
            entityCreate.set("testDate3", 3L, null);
            entityCreate.set("testDouble1", 4L, random.nextDouble());
            entityCreate.set("testDouble2", 5L, null);
            entityCreate.set("testDouble3", 6L, random.nextDouble());
            entityCreate.set("testInteger1", 7L, null);
            entityCreate.set("testInteger2", 8L, random.nextInt());
            entityCreate.set("testInteger3", 9L, random.nextInt());
            long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
            BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

            // DATE
            Set<String> attributeNamesCreate = entityCreate.getPresentDateAttributeNames();
            Set<String> attributeNamesLoad = entityLoad.getPresentDateAttributeNames();
            assertEquals("One of the date values ​​are not saved or loaded. " +
                    "Expected date values count: " + attributeNamesCreate.size() +
                    ", actual date values count: " + attributeNamesLoad.size(),
                    attributeNamesCreate.size(), attributeNamesLoad.size());

            for (String dateAttributeName: attributeNamesCreate)
            {
                IBatchValue batchValue = entityLoad.getBatchValue(dateAttributeName);
                if (batchValue != null)
                {
                    IBatchValue batchValueCreate = entityCreate.getBatchValue(dateAttributeName);
                    assertEquals(
                            "Not properly saved or loaded an index field of one of the date values.",
                            batchValueCreate.getIndex(), batchValue.getIndex());

                    Date valueCreate = (Date)batchValueCreate.getValue();
                    Date valueLoad = (Date)batchValue.getValue();

                    assertTrue(
                            "Not properly saved or loaded date value.",
                            valueCreate == null && valueLoad == null ?
                                    true :
                                    valueCreate != null && valueLoad != null ?
                                            DateUtils.compareBeginningOfTheDay(valueCreate, valueLoad) == 0 :
                                            false);
                }
            }

            // DOUBLE
            attributeNamesCreate = entityCreate.getPresentDoubleAttributeNames();
            attributeNamesLoad = entityLoad.getPresentDoubleAttributeNames();
            assertEquals("One of the double values ​​are not saved or loaded. " +
                    "Expected double values count: " + attributeNamesCreate.size() +
                    ", actual double values count: " + attributeNamesLoad.size(),
                    attributeNamesCreate.size(), attributeNamesLoad.size());

            for (String dateAttributeName: attributeNamesCreate) {
                IBatchValue batchValue = entityLoad.getBatchValue(dateAttributeName);
                if (batchValue != null) {
                    IBatchValue batchValueCreate = entityCreate.getBatchValue(dateAttributeName);
                    assertEquals(
                            "Not properly saved or loaded an index field of one of the double values.",
                            batchValueCreate.getIndex(), batchValue.getIndex());

                    Double valueCreate = (Double)batchValueCreate.getValue();
                    Double valueLoad = (Double)batchValue.getValue();

                    assertTrue(
                            "Not properly saved or loaded double value.",
                            valueCreate == null && valueLoad == null ?
                                    true :
                                    valueCreate != null && valueLoad != null ?
                                            valueCreate.equals(valueLoad) :
                                            false);
                }
            }

            // INTEGER
            attributeNamesCreate = entityCreate.getPresentIntegerAttributeNames();
            attributeNamesLoad = entityLoad.getPresentIntegerAttributeNames();
            assertEquals("One of the integer values ​​are not saved or loaded. " +
                    "Expected integer values count: " + attributeNamesCreate.size() +
                    ", actual integer values count: " + attributeNamesLoad.size(),
                    attributeNamesCreate.size(), attributeNamesLoad.size());

            for (String dateAttributeName: attributeNamesCreate) {
                IBatchValue batchValue = entityLoad.getBatchValue(dateAttributeName);
                if (batchValue != null) {
                    IBatchValue batchValueCreate = entityCreate.getBatchValue(dateAttributeName);
                    assertEquals(
                            "Not properly saved or loaded an index field of one of the integer values.",
                            batchValueCreate.getIndex(), batchValue.getIndex());

                    Integer valueCreate = (Integer)batchValueCreate.getValue();
                    Integer valueLoad = (Integer)batchValue.getValue();

                    assertTrue(
                            "Not properly saved or loaded integer value.",
                            valueCreate == null && valueLoad == null ?
                                    true :
                                    valueCreate != null && valueLoad != null ?
                                            valueCreate.equals(valueLoad) :
                                            false);
                }
            }
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

}
