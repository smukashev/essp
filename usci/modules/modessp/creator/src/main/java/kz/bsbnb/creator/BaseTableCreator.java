package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.sql.DataSource;
import java.sql.SQLException;

public class BaseTableCreator {
    protected MetaClass metaClass;
    protected boolean performDrops;

    public BaseTableCreator(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public BaseTableCreator withStaticTables() {
        return this;
    }

    public BaseTableCreator performDrops() {
        this.performDrops = true;
        return this;
    }

    public void execute(DataSource dataSource) throws SQLException {

    }
}
