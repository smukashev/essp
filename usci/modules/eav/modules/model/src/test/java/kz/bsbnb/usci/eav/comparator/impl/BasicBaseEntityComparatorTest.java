package kz.bsbnb.usci.eav.comparator.impl;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.junit.Test;

import java.util.Date;


public class BasicBaseEntityComparatorTest {
    @Test
    public void compareSetTest() {
        MetaClass metaDocument = new MetaClass( "document" );
        MetaClass metaRefDocType = new MetaClass( "ref_doc_type" );
        metaRefDocType.setMetaAttribute("code", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        IMetaAttribute refDocTypeAttribute = new MetaAttribute(true, false, metaRefDocType);
        refDocTypeAttribute.setImmutable(true);
        metaDocument.setMetaAttribute("doc_type", refDocTypeAttribute);
        metaDocument.setMetaAttribute("no", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        MetaSet metaDocs = new MetaSet(metaDocument);
        BaseEntity refDocType = new BaseEntity(metaRefDocType, new Date(), 0);
        refDocType.put("code", new BaseValue<>("01"));

        BaseEntity document1 = new BaseEntity(metaDocument, new Date(), 0);
        document1.put("doc_type", new BaseValue<>(refDocType));
        document1.put("no", new BaseValue<>("no#1"));

        BaseEntity document2 = new BaseEntity(metaDocument, new Date(), 0);
        document2.put("doc_type", new BaseValue<>(refDocType));
        document2.put("no", new BaseValue<>("no#2"));

        BaseEntity document3 = new BaseEntity(metaDocument, new Date(), 0);
        document3.put("doc_type", new BaseValue<>(refDocType));
        document3.put("no", new BaseValue<>("no#2"));

        BaseSet docs1 = new BaseSet(metaDocs, 0);
        BaseSet docs2 = new BaseSet(metaDocs, 0);

        docs1.put(new BaseValue<>(document1));
        docs2.put(new BaseValue<>(document2));

        MetaClass metaSubject = new MetaClass("subject");
        metaSubject.setMetaAttribute("docs", new MetaAttribute(true, false, metaDocs));

        BaseEntity subject1 = new BaseEntity(metaSubject, new Date(), 0);
        subject1.put("docs", new BaseValue<>(docs1));

        BaseEntity subject2 = new BaseEntity(metaSubject, new Date(), 0);
        subject2.put("docs", new BaseValue<>(docs2));

        // todo: fix this test
        /*
        Assert.assertFalse(subject1.equalsByKey(subject2));
        Assert.assertFalse(subject1.equalsByKey(subject2));

        docs2.put(new BaseValue<>(document1));

        Assert.assertTrue(subject2.equalsByKey(subject1));
        Assert.assertTrue(subject1.equalsByKey(subject2));
        Assert.assertFalse(document1.equalsByKey(document2));
        Assert.assertTrue(document2.equalsByKey(document3));
        */
    }

    @Test
    public void compareDocumentTest() {
        MetaClass metaDocument = new MetaClass( "document" );
        MetaClass metaRefDocType = new MetaClass( "ref_doc_type" );
        metaRefDocType.setMetaAttribute("code", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        IMetaAttribute refDocTypeAttribute = new MetaAttribute(true, false, metaRefDocType);
        refDocTypeAttribute.setImmutable(true);
        metaDocument.setMetaAttribute("doc_type", refDocTypeAttribute);
        metaDocument.setMetaAttribute("no", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaDocument.setMetaAttribute("is_identification", new MetaAttribute(false, true, new
                MetaValue(DataTypes.BOOLEAN)));

        BaseEntity refDocType = new BaseEntity(metaRefDocType, new Date(), 0);
        refDocType.put("code", new BaseValue<>("01"));

        BaseEntity document1 = new BaseEntity(metaDocument, new Date(), 0);
        document1.put("is_identification", new BaseValue<>(false));
        document1.put("doc_type", new BaseValue<>(refDocType));
        document1.put("no", new BaseValue<>("no#1"));

        BaseEntity document2 = new BaseEntity(metaDocument, new Date(), 0);
        document2.put("is_identification", new BaseValue<>(true));
        document2.put("doc_type", new BaseValue<>(refDocType));
        document2.put("no", new BaseValue<>("no#1"));

        Assert.assertTrue(document1.equalsByKey(document2));
    }

    @Test
    public void comparePledgeTest() {
        MetaClass metaPledge = new MetaClass( "pledge" );

        MetaClass metaRefPledgeType = new MetaClass( "ref_pledge_type" );
        metaRefPledgeType.setMetaAttribute("code", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        IMetaAttribute refPledgeTypeAttribute = new MetaAttribute(true, false, metaRefPledgeType);
        refPledgeTypeAttribute.setImmutable(true);
        metaPledge.setMetaAttribute("pledge_type", refPledgeTypeAttribute);
        metaPledge.setMetaAttribute("contract", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        BaseEntity refPledgeType = new BaseEntity(metaRefPledgeType, new Date(), 0);
        refPledgeType.put("code", new BaseValue<>("10"));

        BaseEntity pledge1 = new BaseEntity(metaPledge, new Date(), 0);
        pledge1.put("pledge_type", new BaseValue<>(refPledgeType));
        pledge1.put("contract", new BaseValue<>("no#1"));

        BaseEntity pledge2 = new BaseEntity(metaPledge, new Date(), 0);
        pledge2.put("pledge_type", new BaseValue<>(refPledgeType));
        pledge2.put("contract", new BaseValue<>("no#1"));

        BaseEntity pledge3 = new BaseEntity(metaPledge, new Date(), 0);
        pledge3.put("pledge_type", new BaseValue<>(refPledgeType));
        pledge3.put("contract", new BaseValue<>("no#2"));

        Assert.assertTrue(pledge1.equalsByKey(pledge2));
        Assert.assertFalse(pledge1.equalsByKey(pledge3));

        MetaClass metaCredit = new MetaClass("credit");
        MetaSet pledgeSetMeta = new MetaSet(metaPledge);

        IMetaAttribute pledgeSetAttribute = new MetaAttribute(false, false, pledgeSetMeta);

        metaCredit.setMetaAttribute("pledges", pledgeSetAttribute);

        MetaClass primaryContractMeta = new MetaClass("primary_contract");
        primaryContractMeta.setMetaAttribute("no", new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        primaryContractMeta.setMetaAttribute("date", new MetaAttribute(true, false, new MetaValue(DataTypes.DATE)));

        metaCredit.setMetaAttribute("primary_contract", new MetaAttribute(true, false, primaryContractMeta));

        BaseEntity credit1 = new BaseEntity(metaCredit, new Date());
        BaseEntity credit2 = new BaseEntity(metaCredit, new Date());
        BaseEntity credit3 = new BaseEntity(metaCredit, new Date());

        BaseEntity primaryContract1 = new BaseEntity(primaryContractMeta, new Date());
        primaryContract1.put("no", new BaseValue<>("no#1"));
        primaryContract1.put("date", new BaseValue<>(new Date()));

        BaseEntity primaryContract2 = new BaseEntity(primaryContractMeta, new Date());
        primaryContract2.put("no", new BaseValue<>("no#2"));
        primaryContract2.put("date", new BaseValue<>(new Date()));



        BaseSet pledgeSet1 = new BaseSet(pledgeSetMeta, 0);
        pledgeSet1.put(new BaseValue<>(pledge1));
        pledgeSet1.put(new BaseValue<>(pledge2));

        BaseSet pledgeSet2 = new BaseSet(pledgeSetMeta, 0);
        pledgeSet2.put(new BaseValue<>(pledge3));

        Assert.assertFalse(pledgeSet1.equals(pledgeSet2));

        credit1.put("primary_contract", new BaseValue<>(primaryContract1));
        credit2.put("primary_contract", new BaseValue<>(primaryContract1));
        credit3.put("primary_contract", new BaseValue<>(primaryContract2));

        credit1.put("pledges", new BaseValue(pledgeSet1));
        credit2.put("pledges", new BaseValue(pledgeSet2));
        credit3.put("pledges", new BaseValue(pledgeSet1));

        Assert.assertTrue(credit1.equalsByKey(credit2));
        Assert.assertFalse(credit1.equalsByKey(credit3));


    }
}
