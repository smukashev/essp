package kz.bsbnb.usci.eav.comparator.impl;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.junit.Test;

import java.util.Date;


public class BasicBaseEntityComparatorTest {
    private BasicBaseEntityComparator basicBaseEntityComparator = new BasicBaseEntityComparator();
    @Test
    public void compareSetTest() {
        MetaClass metaDocument = new MetaClass( "document" );
        MetaClass metaRefDocType = new MetaClass( "ref_doc_type" );
        metaRefDocType.setMetaAttribute("code", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaDocument.setMetaAttribute("doc_type", new MetaAttribute(true, false, metaRefDocType));
        metaDocument.setMetaAttribute("no", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        MetaSet metaDocs = new MetaSet(metaDocument);

        BaseEntity refDocType = new BaseEntity(metaRefDocType, new Date());
        refDocType.put("code", new BaseValue<>("01"));

        BaseEntity document1 = new BaseEntity(metaDocument, new Date());
        document1.put("doc_type", new BaseValue<>(refDocType));
        document1.put("no", new BaseValue<>("no#1"));

        BaseEntity document2 = new BaseEntity(metaDocument, new Date());
        document2.put("doc_type", new BaseValue<>(refDocType));
        document2.put("no", new BaseValue<>("no#2"));

        BaseSet docs1 = new BaseSet(metaDocs);
        BaseSet docs2 = new BaseSet(metaDocs);

        docs1.put(new BaseValue<>(document1));
        docs2.put(new BaseValue<>(document1));
        docs2.put(new BaseValue<>(document2));

    }
}
