package kz.bsbnb.usci.eav.persistance.dao.impl;


import static kz.bsbnb.eav.persistance.generated.Tables.EAV_LOGS;

import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import kz.bsbnb.usci.eav.persistance.dao.IBaseLogsDao;

/**
 * Created by Yerlan.Zhumashev on 24.04.2017.
 */

@Repository
public class BaseLogsDaoDaoImpl extends JDBCSupport implements IBaseLogsDao {

    @Autowired
    private DSLContext context;

    @Override
    public void insertLogs(String portletname, String username, String comment) {

        Insert insert = context.insertInto(EAV_LOGS)
               .set(EAV_LOGS.PORTLETNAME, portletname)
               .set(EAV_LOGS.PORTALUSERNAME, username)
               .set(EAV_LOGS.PORTLETCOMMENT, comment);

        long id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }
}
