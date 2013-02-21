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
import org.junit.After;
import org.junit.Before;
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

    @Before
    public void setUp() throws Exception {
        postgreSQLStorageImpl.initialize();
    }

    @After
    public void tearDown() throws Exception {
        postgreSQLStorageImpl.clear();
    }

    private MetaClass generateFullMetaClass()
    {
        long time = Calendar.getInstance().getTimeInMillis();

        MetaClass metaCreate = new MetaClass("testClass");

        //Header
        metaCreate.setBeginDate(new Timestamp(time));
        metaCreate.setDisabled(false);

        //Simple attributes
        metaCreate.setMetaAttribute("testDate",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DATE)));
        metaCreate.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaCreate.setMetaAttribute("testDouble",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DOUBLE)));
        metaCreate.setMetaAttribute("testBoolean",
                new MetaAttribute(false, false, new MetaValue(DataTypes.BOOLEAN)));
        metaCreate.setMetaAttribute("testString",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));

        //Simple array
        metaCreate.setMetaAttribute("testArray", new MetaAttribute(false, false,
                new MetaSet(new MetaValue(DataTypes.DATE))));
        metaCreate.setMetaAttribute("testArray", new MetaAttribute(false, false,
                new MetaSet(new MetaValue(DataTypes.INTEGER))));
        metaCreate.setMetaAttribute("testArray", new MetaAttribute(false, false,
                new MetaSet(new MetaValue(DataTypes.DOUBLE))));
        metaCreate.setMetaAttribute("testArray", new MetaAttribute(false, false,
                new MetaSet(new MetaValue(DataTypes.BOOLEAN))));
        metaCreate.setMetaAttribute("testArray", new MetaAttribute(false, false,
                new MetaSet(new MetaValue(DataTypes.STRING))));

        //Complex attribute
        MetaClass metaClass = new MetaClass("innerClass");
        metaClass.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaClass.setMetaAttribute("testDouble",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DOUBLE)));

        MetaClass metaSubClass = new MetaClass("innerClass1");
        metaSubClass.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaSubClass.setMetaAttribute("testDouble",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DOUBLE)));
        metaClass.setMetaAttribute("testInnerSubClass",
                new MetaAttribute(false, false, metaSubClass));

        metaCreate.setMetaAttribute("testInnerClass",
                new MetaAttribute(false, false, metaClass));

        //Simple array of array of dates
        metaCreate.setMetaAttribute("testArrayArray",
                new MetaAttribute(false, false, new MetaSet(new MetaSet(new MetaValue(DataTypes.DATE)))));

        //Complex array
        MetaClass metaClassForArray = new MetaClass("innerClassForArray");
        metaClassForArray.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaClassForArray.setMetaAttribute("testDouble",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DOUBLE)));
        metaClassForArray.setComplexKeyType(ComplexKeyTypes.ANY);
        metaCreate.setMetaAttribute("testInnerClassArray",
                new MetaAttribute(false, false, new MetaSet(metaClassForArray)));

        return metaCreate;
    }

    @Test
    public void saveMetaClass() throws Exception {
        logger.debug("Create metadata test");

        MetaClass metaCreate = generateFullMetaClass();

        long id;
        MetaClass loadClassNotExists;

        //try to load not existing class
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

        //try to load by id and by name
        MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
        MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

        assertTrue(metaCreate.equals(loadedByNameMetaCreate));
        assertTrue(metaCreate.equals(loadedByIdMetaCreate));
    }

    @Test
    public void updateMetaClass() throws Exception {
        logger.debug("Update metadata test");

        MetaClass metaCreate = generateFullMetaClass();

        postgreSQLMetaClassDaoImpl.save(metaCreate);

        metaCreate.setDisabled(true);
        metaCreate.setBeginDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        metaCreate.getMetaAttribute("testDate").setKey(!metaCreate.getMetaAttribute("testDate").isKey());
        metaCreate.setMetaAttribute("testNewDate", new MetaAttribute(false, false,
                new MetaValue(DataTypes.DATE)));
        metaCreate.removeMemberType("testDouble");

        postgreSQLMetaClassDaoImpl.save(metaCreate);

        MetaClass loaded = postgreSQLMetaClassDaoImpl.load(metaCreate.getId());

        assertTrue(metaCreate.equals(loaded));
    }


    @Test
    public void deleteMetaClass() throws Exception {
        logger.debug("Delete metadata test");

        MetaClass metaCreate = generateFullMetaClass();

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

        MetaClass innerClass = postgreSQLMetaClassDaoImpl.load("innerClass");
        postgreSQLMetaClassDaoImpl.remove(innerClass);
        MetaClass innerClass1 = postgreSQLMetaClassDaoImpl.load("innerClass1");
        postgreSQLMetaClassDaoImpl.remove(innerClass1);
        MetaClass innerClassForArray = postgreSQLMetaClassDaoImpl.load("innerClassForArray");
        postgreSQLMetaClassDaoImpl.remove(innerClassForArray);

        try
        {
            postgreSQLMetaClassDaoImpl.load(metaCreate.getId());
            fail("Loaded removed class");
        }
        catch(IllegalArgumentException e)
        {
        }

        if(!postgreSQLStorageImpl.isClean())
        {
            fail("DB after deletion is not clean!");
        }
    }

    @Test
    public void beginDateMetaClass() throws Exception {
        logger.debug("Create multiple metadata test");

        MetaClass metaCreate = new MetaClass("testClass");

        metaCreate.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));

        Thread.sleep(200);

        MetaClass metaCreateNextDisabled = new MetaClass("testClass");

        metaCreateNextDisabled.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaCreateNextDisabled.setMetaAttribute("testInteger1",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));

        metaCreateNextDisabled.setDisabled(true);

        Thread.sleep(200);

        MetaClass metaCreateNext = new MetaClass("testClass");

        metaCreateNext.setMetaAttribute("testInteger",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));
        metaCreateNext.setMetaAttribute("testInteger2",
                new MetaAttribute(false, false, new MetaValue(DataTypes.INTEGER)));

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
}
