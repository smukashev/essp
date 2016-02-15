package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import org.jooq.Table;

public interface ITable {
    Query getQueries(Long entityId);
    Table getTable();
}
