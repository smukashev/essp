package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 12/06/2015.
 */
public class BACT extends BaseRef {
    public BACT(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        BalanceAccount ba = (BalanceAccount)hm.get("balance_account");
        Element balanceAccount = getDocument().createElement("balance_account");

        appendToElement(balanceAccount, "no_", ba.get("NO_"));
        root.appendChild(balanceAccount);

        CreditType ct = (CreditType)hm.get("credit_type");
        Element creditType = getDocument().createElement("credit_type");
        appendToElement(creditType, "code", ct.get("CODE"));
        root.appendChild(creditType);

    }
}
