package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericTestCase {
    @Autowired
    protected IStorage postgreSQLStorageImpl;

    @Autowired
    protected IBatchRepository batchRepository;

    @Before
    public void setUp() throws Exception {
        if (postgreSQLStorageImpl != null) {
            postgreSQLStorageImpl.clear();
            postgreSQLStorageImpl.initialize();
        }
        if (batchRepository != null) {
            batchRepository.clearCache();
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    protected MetaClass generateMetaClass() {
        MetaClass metaStreetHolder = new MetaClass("street");
        metaStreetHolder.setMetaAttribute("lang",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));

        MetaClass metaHouseHolder = new MetaClass("house");
        metaHouseHolder.setMetaAttribute("value",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.INTEGER))));

        MetaClass metaAddressHolder = new MetaClass("address");
        metaAddressHolder.setMetaAttribute("country",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("city",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("house",
                new MetaAttribute(metaHouseHolder));
        metaAddressHolder.setMetaAttribute("street",
                new MetaAttribute(metaStreetHolder));

        MetaClass metaDocumentHolder = new MetaClass("document1");
        metaDocumentHolder.setMetaAttribute("type",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaDocumentHolder.setMetaAttribute("no",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        MetaClass metaDocumentsHolder = new MetaClass("documents");
        metaDocumentsHolder.setMetaAttribute("document",
                new MetaAttribute(new MetaSet(metaDocumentHolder)));

        MetaClass metaNameHolder = new MetaClass("name");
        metaNameHolder.setMetaAttribute("firstname",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaNameHolder.setMetaAttribute("lastname",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        MetaClass metaSubjectHolder = new MetaClass("subject");
        metaSubjectHolder.setMetaAttribute("name",
                new MetaAttribute(true, false, metaNameHolder));
        metaSubjectHolder.setMetaAttribute("documents",
                new MetaAttribute(metaDocumentsHolder));
        metaSubjectHolder.setMetaAttribute("address",
                new MetaAttribute(metaAddressHolder));

        MetaClass metaContractHolder = new MetaClass("contract");
        metaContractHolder.setMetaAttribute("no",
                new MetaAttribute(true, false, new MetaValue(DataTypes.INTEGER)));
        metaContractHolder.setMetaAttribute("subject",
                new MetaAttribute(true, false, metaSubjectHolder));

        return metaContractHolder;
    }

    protected BaseEntity generateBaseEntity(Batch batch, IMetaClassRepository metaFactory) {
        BaseEntity streetEntity = new BaseEntity(metaFactory.getMetaClass("street"), batch.getRepDate());
        streetEntity.put("lang", new BaseValue(batch, 1, "KAZ"));
        streetEntity.put("value", new BaseValue(batch, 1, "ABAY"));

        BaseEntity houseEntity = new BaseEntity(metaFactory.getMetaClass("house"), batch.getRepDate());
        BaseSet houseSet = new BaseSet(((MetaSet) (houseEntity.getMemberType("value"))).getMemberType());
        houseSet.put(new BaseValue(batch, 2, 111));
        houseSet.put(new BaseValue(batch, 2, 222));
        houseSet.put(new BaseValue(batch, 2, 333));
        houseEntity.put("value", new BaseValue(batch, 2, houseSet));

        BaseEntity addressEntity = new BaseEntity(metaFactory.getMetaClass("address"), batch.getRepDate());
        addressEntity.put("country", new BaseValue(batch, 3, "KAZAKHSTAN"));
        addressEntity.put("city", new BaseValue(batch, 3, "ALMATY"));
        addressEntity.put("street", new BaseValue(batch, 3, streetEntity));
        addressEntity.put("house", new BaseValue(batch, 3, houseEntity));

        BaseEntity documentEntity1 = new BaseEntity(metaFactory.getMetaClass("document1"), batch.getRepDate());
        documentEntity1.put("type", new BaseValue(batch, 4, "RNN"));
        documentEntity1.put("no", new BaseValue(batch, 4, "1234567890"));

        BaseEntity documentEntity2 = new BaseEntity(metaFactory.getMetaClass("document1"), batch.getRepDate());
        documentEntity2.put("type", new BaseValue(batch, 4, "PASSPORT"));
        documentEntity2.put("no", new BaseValue(batch, 4, "0987654321"));

        BaseEntity documentsEntity = new BaseEntity(metaFactory.getMetaClass("documents"), batch.getRepDate());
        BaseSet documentsSet = new BaseSet(((MetaSet) (documentsEntity.getMemberType("document"))).getMemberType());
        documentsSet.put(new BaseValue(batch, 5, documentEntity1));
        documentsSet.put(new BaseValue(batch, 5, documentEntity2));
        documentsEntity.put("document", new BaseValue(batch, 5, documentsSet));

        BaseEntity nameEntity = new BaseEntity(metaFactory.getMetaClass("name"), batch.getRepDate());
        nameEntity.put("firstname", new BaseValue(batch, 6, "KANAT"));
        nameEntity.put("lastname", new BaseValue(batch, 6, "TULBASSIYEV"));

        BaseEntity subjectEntity = new BaseEntity(metaFactory.getMetaClass("subject"), batch.getRepDate());
        subjectEntity.put("name", new BaseValue(batch, 7, nameEntity));
        subjectEntity.put("documents", new BaseValue(batch, 7, documentsEntity));
        subjectEntity.put("address", new BaseValue(batch, 7, addressEntity));

        BaseEntity contractEntity = new BaseEntity(metaFactory.getMetaClass("contract"), batch.getRepDate());
        contractEntity.put("no", new BaseValue(batch, 8, 12345));
        contractEntity.put("subject", new BaseValue(batch, 8, subjectEntity));

        return contractEntity;
    }
}
