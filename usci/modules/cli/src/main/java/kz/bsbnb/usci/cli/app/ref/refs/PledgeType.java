
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class PledgeType extends BaseRef {

    private HashMap hm;

    public PledgeType(HashMap hm){
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
        appendToElement(root,"is_liquid_all",hm.get("IS_LIQUID_ALL"));
        appendToElement(root,"is_liquid_invest",hm.get("IS_LIQUID_INVEST"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
    }
}

