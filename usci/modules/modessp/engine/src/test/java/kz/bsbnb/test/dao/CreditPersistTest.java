package kz.bsbnb.test.dao;

import junit.framework.Assert;
import kz.bsbnb.DataEntity;
import kz.bsbnb.dao.DataEntityDao;
import kz.bsbnb.dao.impl.StaticMetaClassDaoImpl;
import kz.bsbnb.reader.test.ThreePartReader;
import kz.bsbnb.testing.FunctionalTest;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextProp.xml","/applicationContextEngine.xml"})
public class CreditPersistTest extends FunctionalTest {

    @Autowired
    DataEntityDao entityDao;

    ThreePartReader reader;

    @Test
    @Transactional
    public void testInsert() throws Exception {
        reader = new ThreePartReader()
                .withSource(getInputStream("dao/SimpleValues.xml"))
                .withMeta(metaCredit);

        entityDao.setMetaSource(new StaticMetaClassDaoImpl(metaCredit));

        DataEntity entity = reader.read();
        entityDao.insert(entity);
        DataEntity loadedEntity = entityDao.load(entity.getId(), entity.getCreditorId(), entity.getReportDate());
        MetaClass meta = loadedEntity.getMeta();
        Assert.assertEquals("credit", meta.getClassName());
        Assert.assertEquals(entity.getEl("amount"), loadedEntity.getEl("amount"));
        Assert.assertEquals(entity.getEl("maturity_date"), loadedEntity.getEl("maturity_date"));
    }
}
