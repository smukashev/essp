package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 13/04/2015.
 */
public class EconSector extends BaseRef {

    public EconSector(HashMap hm) {
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
    }
}
