package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class BalanceAccount extends BaseRef {
    private HashMap hm;

    public BalanceAccount(HashMap hm){
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
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"display_no",hm.get("DISPLAY_NO"));
        appendToElement(root,"fifth_sign",hm.get("FIFTH_SIGN"));
        appendToElement(root,"first_fourth_signs",hm.get("FIRST_FOURTH_SIGNS"));
        appendToElement(root,"is_in_balance",hm.get("IS_IN_BALANCE"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"no_",hm.get("NO_"));
        appendToElement(root,"seventh_sign",hm.get("SEVENTH_SIGN"));
        appendToElement(root,"sixth_sign",hm.get("SIXTH_SIGN"));

    }
}
