package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.persistance.dao.IBaseLogsDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by emles on 22.08.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContextEAVPersistance.xml")
public class BaseLogsDaoDaoImplTest {

    @Autowired
    IBaseLogsDao iBaseLogsDao;

    @Test
    public void doTest() {
        String portletname = "test-portlet";
        String username = "jvun";
        String comment = "...";
        iBaseLogsDao.insertLogs(portletname, username, comment);
    }

}



