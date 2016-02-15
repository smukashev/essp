package kz.bsbnb.usci.cli.app.exporter;

import org.jooq.Table;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_SET_VALUES;

public class StringSetValues extends ComplexSetValues {
  @Override
  public Table getTable() {
    return EAV_BE_STRING_SET_VALUES;
  }
}
