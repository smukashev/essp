
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class LegalForm extends BaseRef {
    public LegalForm(HashMap hm){
        super(hm);
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
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"short_name_kz",hm.get("SHORT_NAME_KZ"));
        appendToElement(root,"short_name_ru",hm.get("SHORT_NAME_RU"));
    }
}

