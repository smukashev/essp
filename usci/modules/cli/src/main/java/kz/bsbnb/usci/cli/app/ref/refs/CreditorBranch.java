
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import kz.bsbnb.usci.cli.app.ref.craw.DocTypeCrawler;
import kz.bsbnb.usci.cli.app.ref.reps.DocTypeRepository;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Random;


public class CreditorBranch extends BaseRef {

    public CreditorBranch(HashMap hm){
        super(hm);
        if(docTypeCrawler == null) {
            docTypeCrawler = new DocTypeCrawler();
            docTypeCrawler.constructAll();
        }
    }

    public String get(String s){
        return (String) hm.get(s);
    }

    public String getKeyName(){
        return "ID";
    }

    public static DocTypeCrawler docTypeCrawler;



    @Override
    public void buildElement(Element root) {
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"name",hm.get("NAME"));
        appendToElement(root,"short_name",hm.get("SHORT_NAME"));

        CreditorDoc[] cd = (CreditorDoc []) hm.get("docs");
        Element docs = getDocument().createElement("docs");

        root.appendChild(docs);

        DocTypeRepository docTypeRepository = (DocTypeRepository) docTypeCrawler.getRepositoryInstance();


        //case with no documents dummy docs will be appended
        if(cd.length == 0) {
            Element item = getDocument().createElement("item");
            docs.appendChild(item);
            DocType rnn = docTypeRepository.getByCode("11");
            HashMap d = new HashMap();
            d.put("doc_type", rnn);
            d.put("NO_", "000000" + (100000 + Long.valueOf((String)hm.get("ID"))));
            CreditorDoc creditorDoc = new CreditorDoc(d);
            creditorDoc.buildElement(item);
        } else {
            for (int i = 0; i < cd.length; i++) {
                Element item = getDocument().createElement("item");
                docs.appendChild(item);
                cd[i].buildElement(item);
            }
        }

        Element main_office = getDocument().createElement("main_office");
        root.appendChild(main_office);

        Creditor mo = (Creditor) hm.get("main_office");
        mo.buildElement(main_office);

        Region r = (Region) hm.get("region");
        if(r != null) {
            Element region = getDocument().createElement("region");
            root.appendChild(region);
            r.buildElement(region);
        }


    }
}

