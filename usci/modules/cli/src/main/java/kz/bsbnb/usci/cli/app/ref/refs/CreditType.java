
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class CreditType extends BaseRef {

    private HashMap hm;

    public CreditType(HashMap hm){
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
        //appendToElement(root,"kind_id",hm.get("KIND_ID"));
        //appendToElement(root,"shared",hm.get("SHARED"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));

        //appendToElement(root,"shared",hm.get("shared"));
        Element shared = getDocument().createElement("shared");
        root.appendChild(shared);

        Shared s = (Shared) hm.get("shared");
        s.buildElement(shared);

        DebtorType dt = (DebtorType) hm.get("debtor_type");

        if(dt != null){
            Element debtor_type = getDocument().createElement("debtor_type");
            root.appendChild(debtor_type);

            dt.buildElement(debtor_type);
        }
    }
}
