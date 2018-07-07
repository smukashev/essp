package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseTableCreator {
    protected MetaClass metaClass;
    protected boolean performDrops;
    protected boolean createStaticTables;
    protected AtomicInteger namingSequence;

    public BaseTableCreator(MetaClass metaClass) {
        this.metaClass = metaClass;
        this.createStaticTables = false;
        this.performDrops = false;
    }

    public BaseTableCreator withStaticTables() {
        this.createStaticTables = true;
        return this;
    }

    public BaseTableCreator performDrops(boolean value) {
        this.performDrops = value;
        return this;
    }

    public BaseTableCreator withNamingSequence(AtomicInteger namingSequence){
        this.namingSequence = namingSequence;
        return this;
    }

    public void execute(DataSource dataSource) throws SQLException {
        throw new RuntimeException("not implemented");

    }
}
