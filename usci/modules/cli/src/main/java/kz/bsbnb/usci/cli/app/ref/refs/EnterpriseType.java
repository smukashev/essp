
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class EnterpriseType extends BaseRef {
    public EnterpriseType(HashMap hm){
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
        appendToElement(root,"is_organization",hm.get("IS_ORGANIZATION"));
        appendToElement(root,"is_se",hm.get("IS_SE"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
    }
}

