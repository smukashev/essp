package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class Classification extends BaseRef {
    public Classification(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"comment_kz",hm.get("COMMENT_KZ"));
        appendToElement(root,"comment_ru",hm.get("COMMENT_RU"));
        appendToElement(root,"is_input_value",hm.get("IS_INPUT_VALUE"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"provision_debt",hm.get("PROVISION_DEBT"));
        appendToElement(root,"provision_portfolio_max",hm.get("PROVISION_PORTFOLIO_MAX"));
        appendToElement(root,"provision_portfolio_min",hm.get("PROVISION_PORTFOLIO_MIN"));
        appendToElement(root,"value_",hm.get("VALUE_"));
    }
}
