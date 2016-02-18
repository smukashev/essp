package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import java.lang.String;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class EntityExporter {
    int offset = 0;
    //long entityId = 18595555;
    long entityId = 14633;

    @Autowired
    List<ITable> tableList;

    @Autowired
    IBaseEntityLoadDao baseEntityDao;

    public void export(Long entityId){
        Queue<Long> q = new LinkedList<>();
        q.add(entityId);
        List<String> queries = new ArrayList<>();
        while(q.size() > 0) {
            Long id = q.poll();
            IBaseEntity be = baseEntityDao.load(id);
            if(be.getMeta().isReference())
                continue;
            for(ITable table : tableList) {
                Query query = table.getQueries(id);
                queries.addAll(query.getQueries());
                for(Long nextId: query.entityList) {
                    q.add(nextId);
                }
            }
        }

        for(String s: queries) {
            System.out.println(s);
        }
    }

}
