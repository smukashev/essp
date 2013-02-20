package kz.bsbnb.usci.eav.persistance.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BasicBaseEntitySearcherTest {
    @Autowired
    private IBaseEntitySearcher basicBaseEntitySearcher;

    @Test
    public void compare()
    {
        MetaClass meta = new MetaClass("testClass");

        /*meta.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
        meta.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
        meta.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
        meta.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
        meta.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

        MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
        meta.setMemberType("testArray", metaValueArray);

        MetaClass metaClass = new MetaClass("innerClass", false, false);
        metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
        metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
        meta.setMemberType("testInnerClass", metaClass);

        MetaClass metaClassForArray = new MetaClass("innerClassForArray", true, false);
        metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
        metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
        MetaClassArray metaClassArray = new MetaClassArray(metaClassForArray);
        meta.setMemberType("testInnerClassArray", metaClassArray);     */

        BaseEntity b1 = new BaseEntity(meta);
        BaseEntity b2 = new BaseEntity(meta);


        //assertTrue(basicBaseEntitySearcher.compare(b1, b2));
    }
}
