package kz.bsbnb.usci.cli.app.exporter;

import org.jooq.Table;
import org.springframework.stereotype.Component;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_OPTIMIZER;

@Component
public class Optimizer extends AbstractTable {
  @Override
  public Table getTable() {
    return EAV_OPTIMIZER;
  }
}
