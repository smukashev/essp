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

import java.util.Date;

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

            long metaId = postgreSQLMetaClassDaoImpl.save(metaCreate);
            MetaClass metaLoad = postgreSQLMetaClassDaoImpl.load(metaId);

            Batch batchCreate = new Batch();
            long batchId = postgreSQLBatchEntityDaoImpl.save(batchCreate);
            Batch batchLoad = postgreSQLBatchEntityDaoImpl.load(batchId);


            BaseEntity entityCreate = new BaseEntity(metaLoad, batchLoad);
            Date testDate1 = new Date();
            entityCreate.set("testDate1", 1L, testDate1);
            Date testDate2 = new Date();
            entityCreate.set("testDate2", 2L, testDate2);
            Date testDate3 = new Date();
            entityCreate.set("testDate3", 3L, testDate3);
            long entityId = postgreSQLBaseEntityDaoImpl.save(entityCreate);
            BaseEntity entityLoad = postgreSQLBaseEntityDaoImpl.load(entityId);

            for (String dateAttributeName: entityCreate.getPresentSimpleAttributeNames(DataTypes.DATE)) {
                IBatchValue batchValue = entityLoad.getBatchValue(dateAttributeName);
                assertNotNull("One of the date values ​​are not saved or loaded.", batchValue);

                if (batchValue != null) {
                    IBatchValue batchValueCreate = entityCreate.getBatchValue(dateAttributeName);
                    assertEquals(
                            "Not properly saved or loaded an index field of one of the date values.",
                            batchValueCreate.getIndex(), batchValue.getIndex());
                    assertTrue(
                            "Not properly saved or loaded date value.",
                            DateUtils.compareBeginningOfTheDay((Date)batchValueCreate.getValue(), (Date)batchValue.getValue()) == 0);
                }
            }
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

}
