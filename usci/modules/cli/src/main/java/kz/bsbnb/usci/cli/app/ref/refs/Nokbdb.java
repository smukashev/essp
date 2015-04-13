package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 18.12.13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class Nokbdb extends BaseRef {
    public Nokbdb(HashMap hm) {
        super(hm);
    }

    public HashMap getHm() {
        return hm;
    }

    public String get(String s){
       return (String) hm.get(s);
    }

    public String getKeyName(){
        return "CREDITOR_ID";
    }

    @Override
    public String toString() {
        return (String) hm.get("NOKBDB_CODE");
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root, "code", hm.get("NOKBDB_CODE"));
    }
}
