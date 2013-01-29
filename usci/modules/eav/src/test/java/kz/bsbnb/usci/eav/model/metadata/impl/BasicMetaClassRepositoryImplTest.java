package kz.bsbnb.usci.eav.model.metadata.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BasicMetaClassRepositoryImplTest {

    private final Logger logger = LoggerFactory.getLogger(BasicMetaClassRepositoryImplTest.class);

    @Autowired
    BasicMetaClassRepositoryImpl basicMetaClassRepositoryImpl;

    @Autowired
    IStorage storageImpl;
    @Autowired
    IMetaClassDao metaClassDaoImpl;

    @Test
    public void getMetaClass()
    {
        logger.debug("getMetaClass test started");

        try {
            storageImpl.initialize();

            logger.debug("Create metadata test");

            MetaClass metaCreate = new MetaClass("testClass");

            metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass", true, false);
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testInnerClass", metaClass);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray", true, false);
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassArray metaClassArray = new MetaClassArray(metaClassForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);

            long id = metaClassDaoImpl.save(metaCreate);

            MetaClass loadedByNameMetaCreate = basicMetaClassRepositoryImpl.getMetaClass("testClass");

            assertTrue(metaCreate.equals(loadedByNameMetaCreate));
        }
        finally
        {
            storageImpl.clear();
        }
    }
}
