package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.showcase.dao.IShowCaseDao;
import kz.bsbnb.usci.eav.test.GenericTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})
public class ShowCaseHolderTest extends GenericTestCase
{

    @Autowired
    ShowCaseHolder scHolder;

    @Autowired
    protected IMetaClassRepository metaClassRepository;

    @Autowired
    protected IShowCaseDao showCaseDao;

    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    private final Logger logger = LoggerFactory.getLogger(ShowCaseHolderTest.class);

    public ShowCaseHolderTest() {
    }

    @Test
    public void holderTest() throws Exception {
        ShowCase showCase = new ShowCase();
        MetaClass metaClass = generateMetaClass();

        metaClassRepository.saveMetaClass(metaClass);

        // 1 january 2013
        Batch batch = batchRepository.addBatch(new Batch(new Date(new Long("1356976800000"))));

        IBaseEntity entity = generateBaseEntity(batch, metaClassRepository);

        System.out.println(metaClass.toString());

        showCase.setName("TestShowCase");
        showCase.setTableName("TEST");
        showCase.setMeta(metaClass);

        showCase.addField(metaClass, "subject.address", "city");
        showCase.addField(metaClass, "subject.address", "country");
        showCase.addField(metaClass, "subject.address.street", "value", "street");
        showCase.addField(metaClass, "subject.name", "firstname");
        showCase.addField(metaClass, "subject.name", "lastname");

        showCaseDao.save(showCase);

        scHolder.setShowCaseMeta(showCase);

        scHolder.createTables();

        EntityProcessorListenerImpl entityProcessorListener = new EntityProcessorListenerImpl();
        entityProcessorListener.addShowCaseHolder(scHolder);

        baseEntityProcessorDao.setApplyListener(entityProcessorListener);

        entity = baseEntityProcessorDao.process(entity);
        System.out.println(entity.toString());
    }
}
