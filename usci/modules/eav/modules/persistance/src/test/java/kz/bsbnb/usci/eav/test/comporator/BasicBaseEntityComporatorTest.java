package kz.bsbnb.usci.eav.test.comporator;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.comparator.IBaseEntityComparator;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;

/**
 * @author abukabayev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BasicBaseEntityComporatorTest {

    @Autowired
    IBatchDao batchDao;

    @Test
    public void testCompare() {
        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batchDao.save(batch);
        MetaClass meta1 = new MetaClass("testClass1");
        MetaClass meta2 = new MetaClass("testClass2");
        // TODO: Implement generation of the reporting date.
        BaseEntity entity1 = new BaseEntity(meta1, new java.util.Date());
        BaseEntity entity2 = new BaseEntity(meta1, new java.util.Date());
        BaseEntity entity3 = new BaseEntity(meta2, new java.util.Date());

        IBaseEntityComparator instance = new BasicBaseEntityComparator();

        entity1.getMeta().setMetaAttribute("attr1",new MetaAttribute(true,false,new MetaValue(DataTypes.STRING)));
        entity2.getMeta().setMetaAttribute("attr1", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        entity1.put("attr1",new BaseValue(batch,1,"str1"));
        entity2.put("attr1",new BaseValue(batch,1,"str1"));

        Assert.assertTrue(instance.compare(entity1, entity2));

        entity2.put("attr1", new BaseValue(batch, 1, "str2"));

        Assert.assertFalse(instance.compare(entity1, entity2));

        Assert.assertFalse(instance.compare(entity1, entity3));
       // Assert.assertTrue(instance.compare(entity1, entity1));

        entity1.getMeta().setMetaAttribute("attr2", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        entity2.getMeta().setMetaAttribute("attr2", new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        entity2.put("attr2",new BaseValue(batch,1,"str2"));

        Assert.assertFalse(instance.compare(entity1, entity2));




//        boolean q=false;
//        try{
//            Assert.assertTrue(instance.compare(entity1, entity2));
//        }
//        catch(IllegalArgumentException e){
//          q=true;
//        }
//
//        if (!q)
//            fail("Accepts null key attribute");
//              fail("Accepts null key attribute");
    }

    @Test
    public void testCompareSet() throws Exception {
        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batchDao.save(batch);
        MetaClass meta1 = new MetaClass("testClass1");
//        MetaClass meta2 = new MetaClass("testClass2");
        // TODO: Implement generation of the reporting date.
        BaseEntity entity1 = new BaseEntity(meta1, new java.util.Date());
        BaseEntity entity2 = new BaseEntity(meta1, new java.util.Date());

        IBaseEntityComparator instance = new BasicBaseEntityComparator();

        entity1.getMeta().setMetaAttribute("testArrayInteger", new MetaAttribute(true, false, new MetaSet(new MetaValue(DataTypes.INTEGER))));
        entity2.getMeta().setMetaAttribute("testArrayInteger", new MetaAttribute(true, false, new MetaSet(new MetaValue(DataTypes.INTEGER))));
        BaseSet baseSet = new BaseSet(((MetaSet)entity1.getMemberType("testArrayInteger")).getMemberType());
        baseSet.put(new BaseValue(batch,1,11));
        baseSet.put(new BaseValue(batch, 1, 22));
        baseSet.put(new BaseValue(batch, 1, 33));
        entity1.put("testArrayInteger", new BaseValue(batch, 1, baseSet));
        entity2.put("testArrayInteger", new BaseValue(batch, 1, baseSet));

        Assert.assertTrue(instance.compare(entity1, entity2));
        //Assert.assertFalse(instance.compare(entity1, entity2));

        MetaClass meta3 = new MetaClass("testClass3");
        // TODO: Implement generation of the reporting date.
        BaseEntity expEntity = new BaseEntity(meta3, new java.util.Date());

        expEntity.getMeta().setMetaAttribute("testString",new MetaAttribute(true,false,new MetaValue(DataTypes.STRING)));
        expEntity.getMeta().setMetaAttribute("testArrayInteger", new MetaAttribute(true, false, new MetaSet(new MetaValue(DataTypes.INTEGER))));

        BaseSet baseSet2 = new BaseSet(((MetaSet)entity1.getMemberType("testArrayInteger")).getMemberType());
        baseSet2.put(new BaseValue(batch,1,11));
        baseSet2.put(new BaseValue(batch,1,22));
        baseSet2.put(new BaseValue(batch,1,33));
        expEntity.put("testArrayInteger",new BaseValue(batch,1,baseSet2));
        expEntity.put("testString",new BaseValue(batch,1,"str"));


        entity1.getMeta().setMetaAttribute("testComplex", new MetaAttribute(meta3));
        entity1.put("testComplex",new BaseValue(batch,1,expEntity));

        entity2.getMeta().setMetaAttribute("testComplex", new MetaAttribute(meta3));
        entity2.put("testComplex",new BaseValue(batch,1,expEntity));

        Assert.assertTrue(instance.compare(entity1, entity2));

    }
}
