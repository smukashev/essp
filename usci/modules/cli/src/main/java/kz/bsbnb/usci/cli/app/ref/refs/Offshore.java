
package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;


public class Offshore extends BaseRef {
    public Offshore(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root, "code", hm.get("CODE"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));

        Country c = (Country) hm.get("country");

        if(c!=null){
            Element country = getDocument().createElement("country");
            root.appendChild(country);

            c.buildElement(country);
        }

    }
}

