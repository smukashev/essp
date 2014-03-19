package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 18.03.14
 * Time: 19:02
 * To change this template use File | Settings | File Templates.
 */
public class DebtorType extends BaseRef {
    private HashMap hm;

    public DebtorType(HashMap hm){
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
        appendToElement(root, "name_ru", get("NAME_RU"));
        appendToElement(root, "name_kz", get("NAME_KZ"));
    }
}
