package kz.bsbnb.usci.cli.app.exporter.model;

import java.util.List;
import java.lang.String;

public class Query {
    public List<String> queries;
    public List<Long> entityList;

    public Query(List<String> queries, List<Long> entityList){
        this.queries = queries;
        this.entityList = entityList;
    }

    public List<String> getQueries() {
        return queries;
    }

    public List<Long> getEntityList() {
        return entityList;
    }
}
