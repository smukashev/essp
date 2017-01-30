package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IEAVLoggerDao;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static kz.bsbnb.eav.persistance.generated.Tables.*;


@Repository
public class EAVLoggerDaoImpl extends JDBCSupport implements IEAVLoggerDao {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    public void log(IPersistable deletedObject) {

        if(deletedObject instanceof BaseValue) {

            String val = "";
            if(((BaseValue) deletedObject).getValue() instanceof Date) {
                val = DataTypes.formatDate(((Date) ((BaseValue) deletedObject).getValue()));
            } else {
                val = (((BaseValue) deletedObject).getValue().toString());
            }

            val = val.length() > 1024 ? val.substring(0, 1024) : val;

            String stackTrace = "";
            StringWriter sw = new StringWriter();

            try {
                throw new RuntimeException();
            } catch (Exception e) {
                StackTraceElement[] stackTrace1 = e.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTrace1) {
                    if(stackTraceElement.getClassName().contains("bsb"))
                        sw.append(stackTraceElement.toString() + "\n");
                }
            }

            stackTrace = sw.toString();

            long baseContainerId = -1;

            Insert insert = context.insertInto(EAV_LOG_DELETES)
                    .set(EAV_LOG_DELETES.BATCH_ID, ((BaseValue) deletedObject).getBaseContainer().getBatchId())
                    .set(EAV_LOG_DELETES.USER_ID, ((BaseValue) deletedObject).getBaseContainer().getUserId())
                    .set(EAV_LOG_DELETES.BASE_VALUE_ID, deletedObject.getId())
                    .set(EAV_LOG_DELETES.CLASS_NAME, deletedObject.getClass().getSimpleName())
                    .set(EAV_LOG_DELETES.CREDITOR_ID, ((BaseValue) deletedObject).getCreditorId())
                    .set(EAV_LOG_DELETES.CONTAINER_ID, ((BaseValue) deletedObject).getBaseContainer().getId())
                    .set(EAV_LOG_DELETES.REPORT_DATE, DataUtils.convert(((BaseValue) deletedObject).getRepDate()))
                    .set(EAV_LOG_DELETES.VALUE, val)
                    .set(EAV_LOG_DELETES.STACKTRACE, stackTrace)
                    .set(EAV_LOG_DELETES.RECEIPT_DATE, DSL.currentTimestamp());

            insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        }

    }
}
