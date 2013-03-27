package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.postgresql.dao.PostgreSQLBatchDaoImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.sql.Timestamp;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BasicBaseEntitySearcherTest extends GenericTestCase
{

    @Autowired
    IBaseEntitySearcher searcher;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IMetaFactory metaFactory;

	private final Logger logger = LoggerFactory.getLogger(BasicBaseEntitySearcherTest.class);

	public BasicBaseEntitySearcherTest() {
    }

    @Test
    public void searcherTest() throws Exception {
        logger.debug("SearcherTest");

        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()), new java.sql.Date(new java.util.Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass());

        BaseEntity contractEntity = generateBaseEntity(batch, metaFactory);

        long id = baseEntityDao.save(contractEntity);

        searcher.findAll(contractEntity);
    }
}
