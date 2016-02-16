package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.String;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class EntityExporter {
  //int offset = 0;

  @Autowired
  List<ITable> tableList;

  @Autowired
  IBaseEntityLoadDao baseEntityDao;

  @Autowired
  IBaseEntityDao entityDao;

  OutputStream outputStream;

  Logger logger = LoggerFactory.getLogger(EntityExporter.class);

  public void setFile(String fileName) {
    try {
      outputStream = new FileOutputStream(fileName);
    } catch (Exception e) {
      logger.warn("invalid filename: " + fileName + " " + e.getMessage() + "falling back to stdout");
      outputStream = System.out;
    }
  }

  public void export(List<Long> entityList) {
    PrintWriter out = new PrintWriter(outputStream);

    for (Long entityId : entityList) {

      out.println("-- dump with id: " + entityId);

      Queue<Long> q = new LinkedList<>();
      q.add(entityId);
      List<String> queries = new ArrayList<>();
      while (q.size() > 0) {
        Long id = q.poll();
        IMetaClass meta = entityDao.getMetaClass(id);
        if (meta.isReference())
          continue;
        for (ITable table : tableList) {
          Query query = table.getQueries(id);
          queries.addAll(query.getQueries());
          for (Long nextId : query.entityList) {
            q.add(nextId);
          }
        }
      }

      for (String s : queries) {
        out.println(s);
      }
    }

    out.close();
  }
}
