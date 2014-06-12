package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: bauyrzhan.makhambeto
 * Date: 29.05.14
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})
public class ShowCaseHolderArrayTest {

    @Autowired
    IBaseEntityProcessorDao entityDao;



    @Autowired
    ShowCaseHolder scHolder;

    @Test
    public void testFirst() throws Exception {
        IBaseEntity entity = entityDao.load(15L);


        ShowCase showCase = new ShowCase();
        showCase.setName("TestShowCaseBKMN");
        showCase.setTableName("TESTBKMN");
        showCase.setMeta(entity.getMeta());

        System.out.println(entity.getMeta());



        showCase.addField(entity.getMeta(), "friends", "firstname","PersonName");
        showCase.addField(entity.getMeta(), "colleagues", "lastname");
        showCase.addField(entity.getMeta(), "friends","contacts","value123");
        showCase.addField(entity.getMeta(),"colleagues.documents","no");
        showCase.addField(entity.getMeta(),"colleagues.documents","desc","Opisalovka");

        scHolder.setShowCaseMeta(showCase);

        scHolder.createTables();
        scHolder.generatePaths();
        scHolder.print(entity);

        System.out.println(entity);
    }

    @Test
    public void testSecond() throws Exception {
        IBaseEntity entity = entityDao.load(7L);


        ShowCase showCase = new ShowCase();
        showCase.setName("TestShowCaseBKMN");
        showCase.setTableName("TESTBKMN");
        showCase.setMeta(entity.getMeta());

        System.out.println(entity.getMeta());



        showCase.addField(entity.getMeta(), "users", "name","PersonName");
        showCase.addField(entity.getMeta(), "subject.users", "name","name");
        showCase.addField(entity.getMeta(), "subject.users", "surname","surname");


        scHolder.setShowCaseMeta(showCase);

        scHolder.createTables();
        scHolder.generatePaths();
        scHolder.print(entity);

        System.out.println(entity);
    }
}



