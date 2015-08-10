package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

public class Meta extends BaseRef {
    public Meta(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {

        appendToElement(root, "name", hm.get("NAME"));
        appendToElement(root, "title", hm.get("TITLE"));
        appendToElement(root, "is_reference", hm.get("IS_REFERENCE"));
        //root.setAttribute("name", ((String) hm.get("NAME")));

//        appendToElement(balanceAccount, "no_", ba.get("NO_"));
//        root.appendChild(balanceAccount);
//
//        CreditType ct = (CreditType)hm.get("credit_type");
//        Element creditType = getDocument().createElement("credit_type");
//        appendToElement(creditType, "code", ct.get("CODE"));
//        root.appendChild(creditType);

    }
}
