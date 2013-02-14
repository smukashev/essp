/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author a.tkachenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLMetaClassDaoImplTest {
	
	@Autowired
    IStorage postgreSQLStorageImpl;
	@Autowired
    IMetaClassDao postgreSQLMetaClassDaoImpl;
	
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);
	
	public PostgreSQLMetaClassDaoImplTest() {
    }

    @Test
    public void saveMetaClass() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Create metadata test");

            MetaClass metaCreate = new MetaClass("testClass");

            metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass");
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassHolder metaClassHolder = new MetaClassHolder(metaClass, true, false);
            metaCreate.setMemberType("testInnerClass", metaClassHolder);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray");
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaClassForArray.setComplexKeyType(ComplexKeyTypes.ANY);
            MetaClassHolder metaClassHolderForArray = new MetaClassHolder(metaClassForArray, true, false);
            MetaClassArray metaClassArray = new MetaClassArray(metaClassHolderForArray);
            //metaClassArray.setComplexKeyType(ComplexKeyTypes.ANY);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);

            long id;
            MetaClass loadClassNotExists;

            try
            {
                loadClassNotExists = postgreSQLMetaClassDaoImpl.load("testClass");
            }
            catch (IllegalArgumentException e)
            {
                logger.debug("Can't load not existing class, and that's ok");
                loadClassNotExists = null;
            }
            assertTrue(loadClassNotExists == null);

            id = postgreSQLMetaClassDaoImpl.save(metaCreate);

            MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
            MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

            assertTrue(metaCreate.equals(loadedByNameMetaCreate));
            assertTrue(metaCreate.equals(loadedByIdMetaCreate));
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void loadMetaClass() throws Exception {
        try {
        	postgreSQLStorageImpl.initialize();
	        
	        logger.debug("Load metadata test");

            long time = Calendar.getInstance().getTimeInMillis();

            MetaClass metaCreate = new MetaClass("testClass");
            metaCreate.setBeginDate(new Timestamp(time));
            metaCreate.setDisabled(false);
	        
	        metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
	        metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
	        metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass");
            MetaClassHolder metaClassHolder = new MetaClassHolder(metaClass, true, false);
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testInnerClass", metaClassHolder);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray");
            MetaClassHolder metaClassHolderForArray = new MetaClassHolder(metaClassForArray, true, false);
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassArray metaClassArray = new MetaClassArray(metaClassHolderForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);
	        
            long id;

            MetaClass loadClassNotExists;

            try
            {
                loadClassNotExists = postgreSQLMetaClassDaoImpl.load("testClass");
            }
            catch (IllegalArgumentException e)
            {
                logger.debug("Class not found, but it's ok.");
                loadClassNotExists = null;
            }

            assertTrue(loadClassNotExists == null);

	        id = postgreSQLMetaClassDaoImpl.save(metaCreate);

	        MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
	        MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

	        assertTrue(metaCreate.equals(loadedByNameMetaCreate));
	        assertTrue(metaCreate.equals(loadedByIdMetaCreate));
        }
        finally
        {
        	postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void updateMetaClass() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Update metadata test");

            long time = Calendar.getInstance().getTimeInMillis();

            MetaClass metaCreate = new MetaClass("testClass");
            metaCreate.setBeginDate(new Timestamp(time));
            metaCreate.setDisabled(false);

            metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass");
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassHolder metaClassHolder = new MetaClassHolder(metaClass, true, false);
            metaCreate.setMemberType("testInnerClass", metaClassHolder);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray");
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassHolder metaClassHolderForArray = new MetaClassHolder(metaClassForArray, true, false);
            MetaClassArray metaClassArray = new MetaClassArray(metaClassHolderForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);

            postgreSQLMetaClassDaoImpl.save(metaCreate);

            metaCreate.setDisabled(true);
            metaCreate.setBeginDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));

            metaCreate.getMemberType("testDate").setKey(true);
            metaCreate.setMemberType("testNewDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.removeMemberType("testDouble");

            postgreSQLMetaClassDaoImpl.save(metaCreate);

            MetaClass loaded = postgreSQLMetaClassDaoImpl.load(metaCreate.getId());

            assertTrue(metaCreate.equals(loaded));
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void deleteMetaClass() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Delete metadata test");

            long time = Calendar.getInstance().getTimeInMillis();

            MetaClass metaCreate = new MetaClass("testClass");
            metaCreate.setBeginDate(new Timestamp(time));
            metaCreate.setDisabled(false);

            metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass");
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassHolder metaClassHolder = new MetaClassHolder(metaClass, true, false);
            metaCreate.setMemberType("testInnerClass", metaClassHolder);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray");
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassHolder metaClassHolderForArray = new MetaClassHolder(metaClassForArray, true, false);
            MetaClassArray metaClassArray = new MetaClassArray(metaClassHolderForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);

            postgreSQLMetaClassDaoImpl.save(metaCreate);

            try
            {
                postgreSQLMetaClassDaoImpl.load(metaCreate.getId());
            }
            catch(IllegalArgumentException e)
            {
                fail(String.format("Can't load MetaClass: %s", e.getMessage()));
            }

            postgreSQLMetaClassDaoImpl.remove(metaCreate);

            try
            {
                postgreSQLMetaClassDaoImpl.load(metaCreate.getId());
                fail("Loaded removed class");
            }
            catch(IllegalArgumentException e)
            {
            }
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void beginDateMetaClass() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Create multiple metadata test");

            MetaClass metaCreate = new MetaClass("testClass");

            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));

            Thread.sleep(200);

            MetaClass metaCreateNextDisabled = new MetaClass("testClass");

            metaCreateNextDisabled.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreateNextDisabled.setMemberType("testInteger1", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreateNextDisabled.setDisabled(true);

            Thread.sleep(200);

            MetaClass metaCreateNext = new MetaClass("testClass");

            metaCreateNext.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreateNext.setMemberType("testInteger2", new MetaValue(DataTypes.INTEGER, false, false));

            postgreSQLMetaClassDaoImpl.save(metaCreate);
            postgreSQLMetaClassDaoImpl.save(metaCreateNext);
            postgreSQLMetaClassDaoImpl.save(metaCreateNextDisabled);

            Thread.sleep(200);

            MetaClass loadedMetaCreateNext = postgreSQLMetaClassDaoImpl.load(metaCreateNext.getClassName());

            assertTrue(metaCreateNext.equals(loadedMetaCreateNext));

            MetaClass loadedMetaCreate = postgreSQLMetaClassDaoImpl.load(metaCreateNextDisabled.getClassName(),
                    metaCreateNextDisabled.getBeginDate());

            assertTrue(metaCreate.equals(loadedMetaCreate));
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }
}
