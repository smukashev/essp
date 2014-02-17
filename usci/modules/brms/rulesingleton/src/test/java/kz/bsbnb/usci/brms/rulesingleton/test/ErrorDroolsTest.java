package kz.bsbnb.usci.brms.rulesingleton.test;

import kz.bsbnb.usci.brms.rulesingleton.BRMSHelper;
import kz.bsbnb.usci.brms.rulesingleton.RulePackageError;
import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;

import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextErrorTest.xml"})
public class ErrorDroolsTest
{
    @Autowired
    RulesSingleton rules;

    MetaClass metaStreetHolder;
    MetaClass metaHouseHolder;
    MetaClass metaAddressHolder;
    MetaClass metaDocumentHolder;
    MetaClass metaDocumentsHolder;
    MetaClass metaNameHolder;
    MetaClass metaSubjectHolder;
    MetaClass metaContractHolder;

    protected MetaClass generateMetaClass()
    {
        metaStreetHolder = new MetaClass("street");
        metaStreetHolder.setMetaAttribute("lang",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));

        metaHouseHolder = new MetaClass("house");
        metaHouseHolder.setMetaAttribute("value",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.INTEGER))));

        metaAddressHolder = new MetaClass("address");
        metaAddressHolder.setMetaAttribute("country",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("city",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("house",
                new MetaAttribute(metaHouseHolder));
        metaAddressHolder.setMetaAttribute("street",
                new MetaAttribute(metaStreetHolder));

        metaDocumentHolder = new MetaClass("document");
        metaDocumentHolder.setMetaAttribute("type",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaDocumentHolder.setMetaAttribute("no",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        metaDocumentsHolder = new MetaClass("documents");
        metaDocumentsHolder.setMetaAttribute("document",
                new MetaAttribute(new MetaSet(metaDocumentHolder)));

        metaNameHolder = new MetaClass("name");
        metaNameHolder.setMetaAttribute("firstname",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaNameHolder.setMetaAttribute("lastname",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        metaSubjectHolder = new MetaClass("subject");
        metaSubjectHolder.setMetaAttribute("name",
                new MetaAttribute(true, false, metaNameHolder));
        metaSubjectHolder.setMetaAttribute("documents",
                new MetaAttribute(metaDocumentsHolder));
        metaSubjectHolder.setMetaAttribute("address",
                new MetaAttribute(metaAddressHolder));

        metaContractHolder = new MetaClass("contract");
        metaContractHolder.setMetaAttribute("no",
                new MetaAttribute(true, false, new MetaValue(DataTypes.INTEGER)));
        metaContractHolder.setMetaAttribute("subject",
                new MetaAttribute(true, false, metaSubjectHolder));

        return metaContractHolder;
    }

    protected BaseEntity generateBaseEntity(Batch batch)
    {
        java.util.Date reportDate = new java.util.Date();

        BaseEntity streetEntity = new BaseEntity(metaStreetHolder, reportDate);
        streetEntity.put("lang", new BaseValue(batch, 1, "KAZ"));
        streetEntity.put("value", new BaseValue(batch, 1, "ABAY"));

        BaseEntity houseEntity = new BaseEntity(metaHouseHolder, reportDate);
        BaseSet houseSet = new BaseSet(((MetaSet)(houseEntity.getMemberType("value"))).getMemberType());
        houseSet.put(new BaseValue(batch, 2, 111));
        houseSet.put(new BaseValue(batch, 2, 222));
        houseSet.put(new BaseValue(batch, 2, 333));
        houseEntity.put("value", new BaseValue(batch, 2, houseSet));

        BaseEntity addressEntity = new BaseEntity(metaAddressHolder, reportDate);
        addressEntity.put("country", new BaseValue(batch, 3, "KAZAKHSTAN"));
        addressEntity.put("city", new BaseValue(batch, 3, "ALMATY"));
        addressEntity.put("street", new BaseValue(batch, 3, streetEntity));
        addressEntity.put("house", new BaseValue(batch, 3, houseEntity));

        BaseEntity documentEntity1 = new BaseEntity(metaDocumentHolder, reportDate);
        documentEntity1.put("type", new BaseValue(batch, 4, "RNN"));
        documentEntity1.put("no", new BaseValue(batch, 4, "600720355581"));

        BaseEntity documentEntity2 = new BaseEntity(metaDocumentHolder, reportDate);
        documentEntity2.put("type", new BaseValue(batch, 4, "PASSPORT"));
        documentEntity2.put("no", new BaseValue(batch, 4, "0987654321"));

        BaseEntity documentsEntity = new BaseEntity(metaDocumentsHolder, reportDate);
        BaseSet documentsSet = new BaseSet(((MetaSet)(documentsEntity.getMemberType("document"))).getMemberType());
        documentsSet.put(new BaseValue(batch, 5, documentEntity1));
        documentsSet.put(new BaseValue(batch, 5, documentEntity2));
        documentsEntity.put("document", new BaseValue(batch, 5, documentsSet));

        BaseEntity nameEntity = new BaseEntity(metaNameHolder, reportDate);
        nameEntity.put("firstname", new BaseValue(batch, 6, "KANAT"));
        nameEntity.put("lastname", new BaseValue(batch, 6, "TULBASSIYEV"));

        BaseEntity subjectEntity = new BaseEntity(metaSubjectHolder, reportDate);
        subjectEntity.put("name", new BaseValue(batch, 7, nameEntity));
        subjectEntity.put("documents", new BaseValue(batch, 7, documentsEntity));
        subjectEntity.put("address", new BaseValue(batch, 7, addressEntity));

        BaseEntity contractEntity = new BaseEntity(metaContractHolder, reportDate);
        contractEntity.put("no", new BaseValue(batch, 8, 12345));
        contractEntity.put("subject", new BaseValue(batch, 8, subjectEntity));

        return contractEntity;
    }

    @Test
    public void errorRulesTest() throws Exception
    {
        assertTrue(rules != null);
        rules.reloadCache();

        for (RulePackageError rpe : rules.getRulePackageErrors())
        {
            System.out.println("Errors: " + rpe.getErrorMsg());
        }

        System.out.println(generateMetaClass().toString());
        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batch.setId(1);
        System.out.println("------------------------");
        BaseEntity entity = generateBaseEntity(batch);
        System.out.println(entity.toString());

        System.out.println("Run package drl");
        entity.clearValidationErrors();
        rules.runRules(entity, "drl");

        System.out.println("===================");
        for (String str : entity.getValidationErrors())
        {
            System.out.println(str);
        }
        System.out.println("===================");
    }

    public RulesSingleton getRules()
    {
        return rules;
    }

    public void setRules(RulesSingleton rules)
    {
        this.rules = rules;
    }
}