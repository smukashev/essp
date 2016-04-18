package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dtulendiyev on 3/28/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContextEAVPersistance.xml")
public class BaseEntityDaoImplTest {

    @Autowired
    private IBaseEntityDao baseEntityDao;

    @Test
    public void testSampleServiceGetOrder() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date reportDate = null;
        try {
            reportDate = sdf.parse("01.09.2015");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        IBaseEntity baseEntity = baseEntityDao.load((long)13583, reportDate);


        baseEntityDao.delete(baseEntity);

    }
}
