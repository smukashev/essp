package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class BankRelation extends BaseRef {
    public BankRelation(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
    }
}
