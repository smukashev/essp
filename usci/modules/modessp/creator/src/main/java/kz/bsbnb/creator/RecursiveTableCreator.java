package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;

import javax.sql.DataSource;
import java.sql.SQLException;

public class RecursiveTableCreator extends BaseTableCreator {
    public RecursiveTableCreator(MetaClass metaCredit) {
        super(metaCredit);
    }

    @Override
    public void execute(DataSource dataSource) throws SQLException {
        SingleTableCreator root = new SingleTableCreator(metaClass);
        root.execute(dataSource);

        for (String attribute : metaClass.getAttributeNames()) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaType.isComplex()) {
                if(metaType.isSet()) {
                    MetaSet childMetaSet = (MetaSet) metaType;
                    MetaClass childMeta = (MetaClass) childMetaSet.getMemberType();
                    new RecursiveTableCreator(childMeta).execute(dataSource);
                } else {
                    MetaClass childMeta = (MetaClass) metaType;
                    new RecursiveTableCreator(childMeta).execute(dataSource);
                }
            }
        }
    }
}
