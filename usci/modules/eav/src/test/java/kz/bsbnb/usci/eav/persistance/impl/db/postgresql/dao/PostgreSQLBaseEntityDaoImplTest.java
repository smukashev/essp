/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
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

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLBaseEntityDaoImplTest {

	@Autowired
    IStorage postgreSQLStorageImpl;
	@Autowired
    IMetaClassDao postgreSQLMetaClassDaoImpl;
    @Autowired
    IMetaClassDao postgreSQLBaseEntityDaoImpl;

	private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

	public PostgreSQLBaseEntityDaoImplTest() {
    }

    @Test
    public void saveBaseEntity() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Create base entity test.");



        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

}
