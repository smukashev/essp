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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;

/**
 *
 * @author a.motov
 */
public class MetaClassToStringTest
{
	private final Logger logger = LoggerFactory.getLogger(MetaClassToStringTest.class);

	public MetaClassToStringTest() {
    }

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
        metaContractHolder = new MetaClass( "contract" );
        metaContractHolder.setMetaAttribute("no" , new MetaAttribute( true , false , new MetaValue(DataTypes.INTEGER)));
        metaSubjectHolder = new MetaClass( "subject" );
        metaAddressHolder = new MetaClass( "address" );
        metaAddressHolder.setMetaAttribute("city" , new MetaAttribute( false , true , new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("country" , new MetaAttribute( false , true , new MetaValue(DataTypes.STRING)));
        metaHouseHolder = new MetaClass( "house" );
        metaHouseHolder.setMetaAttribute("value" , new MetaAttribute( false , true ,  new MetaSet(new MetaValue(DataTypes.INTEGER))));
        metaAddressHolder.setMetaAttribute("house"  , new MetaAttribute( false,true , metaHouseHolder));
        metaStreetHolder = new MetaClass( "street" );
        metaStreetHolder.setMetaAttribute("lang" , new MetaAttribute( false , false , new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value" , new MetaAttribute( false , false , new MetaValue(DataTypes.STRING)));
        metaAddressHolder.setMetaAttribute("street"  , new MetaAttribute( false,true , metaStreetHolder));
        metaSubjectHolder.setMetaAttribute("address"  , new MetaAttribute( false,true , metaAddressHolder));
        metaDocumentsHolder = new MetaClass( "documents" );
        metaDocumentHolder = new MetaClass( "document" );
        metaDocumentHolder.setMetaAttribute("no" , new MetaAttribute( true , false , new MetaValue(DataTypes.STRING)));
        metaDocumentHolder.setMetaAttribute("type" , new MetaAttribute( true , false , new MetaValue(DataTypes.STRING)));
        metaDocumentsHolder.setMetaAttribute("document" , new MetaAttribute( new MetaSet( metaDocumentHolder)));
        metaSubjectHolder.setMetaAttribute("documents"  , new MetaAttribute( false,true , metaDocumentsHolder));
        metaNameHolder = new MetaClass( "name" );
        metaNameHolder.setMetaAttribute("firstname" , new MetaAttribute( true , false , new MetaValue(DataTypes.STRING)));
        metaNameHolder.setMetaAttribute("lastname" , new MetaAttribute( true , false , new MetaValue(DataTypes.STRING)));
        metaSubjectHolder.setMetaAttribute("name"  , new MetaAttribute( true,false , metaNameHolder));
        metaContractHolder.setMetaAttribute("subject"  , new MetaAttribute( true,false , metaSubjectHolder));

        return metaContractHolder;
    }

    protected BaseEntity generateBaseEntity(Batch batch)
    {
        java.util.Date reportDate = new java.util.Date();

        BaseEntity contractEntity = new BaseEntity(metaContractHolder, reportDate);
        BaseEntity subjectEntity = new BaseEntity(metaSubjectHolder, reportDate);
        BaseEntity documentsEntity = new BaseEntity(metaDocumentsHolder, reportDate);
        BaseSet documentsSet = new BaseSet(((MetaSet)( documentsEntity.getMemberType("document"))).getMemberType());
        BaseEntity documentEntity = new BaseEntity(metaDocumentHolder, reportDate);
        documentEntity.put( "no" , new BaseValue(batch, 4 ,"1234567890"));
        documentEntity.put( "type" , new BaseValue(batch, 4 ,"RNN"));
        documentsSet.put(new BaseValue(batch,5,documentEntity));
        BaseEntity documentEntity1 = new BaseEntity(metaDocumentHolder, reportDate);
        documentEntity1.put( "no" , new BaseValue(batch, 4 ,"0987654321"));
        documentEntity1.put( "type" , new BaseValue(batch, 4 ,"PASSPORT"));
        documentsSet.put(new BaseValue(batch,5,documentEntity1));
        documentsEntity.put( "document" , new BaseValue(batch, 5 ,documentsSet));
        subjectEntity.put( "documents" , new BaseValue(batch, 7 ,documentsEntity));
        BaseEntity addressEntity = new BaseEntity(metaAddressHolder, reportDate);
        BaseEntity streetEntity = new BaseEntity(metaStreetHolder, reportDate);
        streetEntity.put( "value" , new BaseValue(batch, 1 ,"ABAY"));
        streetEntity.put( "lang" , new BaseValue(batch, 1 ,"KAZ"));
        addressEntity.put( "street" , new BaseValue(batch, 3 ,streetEntity));
        BaseEntity houseEntity = new BaseEntity(metaHouseHolder, reportDate);
        BaseSet houseSet = new BaseSet(((MetaSet)( houseEntity.getMemberType("value"))).getMemberType());
        houseSet.put(new BaseValue(batch,2,111));
        houseSet.put(new BaseValue(batch,2,222));
        houseSet.put(new BaseValue(batch,2,333));
        houseEntity.put( "value" , new BaseValue(batch, 2 ,houseSet));
        addressEntity.put( "house" , new BaseValue(batch, 3 ,houseEntity));
        addressEntity.put( "country" , new BaseValue(batch, 3 ,"KAZAKHSTAN"));
        addressEntity.put( "city" , new BaseValue(batch, 3 ,"ALMATY"));
        subjectEntity.put( "address" , new BaseValue(batch, 7 ,addressEntity));
        BaseEntity nameEntity = new BaseEntity(metaNameHolder, reportDate);
        nameEntity.put( "lastname" , new BaseValue(batch, 6 ,"TULBASSIYEV"));
        nameEntity.put( "firstname" , new BaseValue(batch, 6 ,"KANAT"));
        subjectEntity.put( "name" , new BaseValue(batch, 7 ,nameEntity));
        contractEntity.put( "subject" , new BaseValue(batch, 8 ,subjectEntity));
        contractEntity.put( "no" , new BaseValue(batch, 8 ,12345));

        return contractEntity;
    }

    @Test
    public void toStringTest() throws Exception {
        logger.debug("MetaClass.toString() test");

        System.out.println(generateMetaClass().toString());
        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batch.setId(1);
        System.out.println("------------------------");
        System.out.println(generateBaseEntity(batch).toString());
    }


    @Test
    public void toJavaTest() throws Exception {
        logger.debug("MetaClass.toString() test");

        System.out.println(generateMetaClass().getJavaFunction("generateMetaClass"));
        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batch.setId(1);
        System.out.println("------------------------");
        System.out.println(generateBaseEntity(batch).toJava("generateBaseEntity"));
    }

}
