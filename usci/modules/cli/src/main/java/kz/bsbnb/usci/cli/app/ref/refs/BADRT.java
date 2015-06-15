package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 13/06/2015.
 */
public class BADRT  extends BaseRef {
    public BADRT(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        //BalanceAccount ba = (BalanceAccount)hm.get("balance_account");
        Element balanceAccount = getDocument().createElement("balance_account");

        appendToElement(balanceAccount, "no_", hm.get("NO_"));
        root.appendChild(balanceAccount);


        Element creditType = getDocument().createElement("debt_remains_type");
        appendToElement(creditType, "code", hm.get("CODE"));
        root.appendChild(creditType);

    }

}
