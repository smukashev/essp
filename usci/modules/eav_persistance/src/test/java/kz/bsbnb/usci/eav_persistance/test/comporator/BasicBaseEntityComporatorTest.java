package kz.bsbnb.usci.eav_persistance.test.comporator;

import junit.framework.Assert;
import kz.bsbnb.usci.eav_model.comparator.IBaseEntityComparator;
import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.fail;

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
        Batch batch = new Batch();
        batchDao.save(batch);
        MetaClass meta1 = new MetaClass("testClass1");
        MetaClass meta2 = new MetaClass("testClass2");
        BaseEntity entity1 = new BaseEntity(meta1);
        BaseEntity entity2 = new BaseEntity(meta2);

        IBaseEntityComparator instance = new BasicBaseEntityComparator();

        entity1.getMeta().setMetaAttribute("attr1",new MetaAttribute(true,false,new MetaValue(DataTypes.STRING)));
        entity2.getMeta().setMetaAttribute("attr1", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        entity1.put("attr1",new BaseValue(batch,1,"str1"));
        entity2.put("attr1",new BaseValue(batch,1,"str1"));

        Assert.assertFalse(instance.compare(entity1, entity2));
        Assert.assertTrue(instance.compare(entity1, entity1));

        entity1.getMeta().setMetaAttribute("attr2",new MetaAttribute(true,false,new MetaValue(DataTypes.STRING)));
        entity2.getMeta().setMetaAttribute("attr2",new MetaAttribute(false,false,new MetaValue(DataTypes.STRING)));
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
    }
}
