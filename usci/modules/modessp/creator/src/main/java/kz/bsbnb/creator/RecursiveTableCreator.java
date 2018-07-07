package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursiveTableCreator extends BaseTableCreator {
    public RecursiveTableCreator(MetaClass metaCredit) {
        super(metaCredit);
    }

    @Override
    public void execute(DataSource dataSource) throws SQLException {
        if(namingSequence == null)
            namingSequence = new AtomicInteger(0);

        new SingleTableCreator(metaClass)
                .withNamingSequence(namingSequence)
                .performDrops(performDrops)
                .execute(dataSource);

        if(createStaticTables) {
            new StaticTableCreator(metaClass)
                    .performDrops(performDrops)
                    .execute(dataSource);
        }

        for (String attribute : metaClass.getAttributeNames()) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaType.isComplex()) {
                if(metaType.isSet()) {
                    MetaSet childMetaSet = (MetaSet) metaType;
                    MetaClass childMeta = (MetaClass) childMetaSet.getMemberType();
                    new RecursiveTableCreator(childMeta)
                            .performDrops(performDrops)
                            .withNamingSequence(namingSequence)
                            .execute(dataSource);
                } else {
                    MetaClass childMeta = (MetaClass) metaType;
                    new RecursiveTableCreator(childMeta)
                            .performDrops(performDrops)
                            .withNamingSequence(namingSequence)
                            .execute(dataSource);
                }
            }
        }
    }
}
