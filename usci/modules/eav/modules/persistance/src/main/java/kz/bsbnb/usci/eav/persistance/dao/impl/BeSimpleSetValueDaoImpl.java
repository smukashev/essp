package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleSetValueDao;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 *
 */
@Repository
public class BeSimpleSetValueDaoImpl extends AbstractBeSetValueDaoImpl implements IBeSimpleSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;


    @Override
    protected long save(long level, boolean last) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected long save(long baseEntityId, long metaAttributeId, long baseSetId, long batchId, long index, Date reportDate, boolean closed, boolean last) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected long save(long parentSetId, long childSetId, long batchId, long index, Date reportDate, boolean closed, boolean last) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected long save(long setId, long batchId, long index, Date reportDate, Object value, boolean closed, boolean last) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
