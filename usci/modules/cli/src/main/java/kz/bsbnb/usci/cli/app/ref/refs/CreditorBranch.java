
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class CreditorBranch extends BaseRef {

    private HashMap hm;

    public CreditorBranch(HashMap hm){
        this.hm = hm;
    }

    public String get(String s){
        return (String) hm.get(s);
    }

    public String getKeyName(){
        return "ID";
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"name",hm.get("NAME"));
        appendToElement(root,"short_name",hm.get("SHORT_NAME"));

        CreditorDoc[] cd = (CreditorDoc []) hm.get("docs");
        Element docs = getDocument().createElement("docs");

        root.appendChild(docs);

        for(int i=0;i<cd.length;i++)
        {
            Element item = getDocument().createElement("item");
            docs.appendChild(item);
            cd[i].buildElement(item);
        }

        Element main_office = getDocument().createElement("main_office");
        root.appendChild(main_office);

        Creditor mo = (Creditor) hm.get("main_office");
        mo.buildElement(main_office);


    }
}

