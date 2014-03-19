package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class SubjectType extends BaseRef {
    private HashMap hm;

    public SubjectType(HashMap hm){
        this.hm = hm;
    }

    public String get(String s){
        return (String) hm.get(s);
    }

    public String getKeyName(){
        return "ID";
    }

    @Override
    public String asXml(int cnt) {
        currentXML = new StringBuilder();
        appendToXml("<name_kz>","</name_kz>",(String) hm.get("NAME_KZ"),cnt);
        appendToXml("<name_ru>","</name_ru>",(String) hm.get("NAME_RU"),cnt);
        appendToXml("<kind_id>","</kind_id>",(String) hm.get("KIND_ID"),cnt);
        appendToXml("<code>","</code>",(String) hm.get("CODE"),cnt);
        appendToXml("<report_period_duration_months>","</report_period_duration_months>",(String) hm.get("REPORT_PERIOD_DURATION_MONTHS"),cnt);


        return currentXML.toString();
    }

    public void print(int shft) {
        for (Object k : hm.keySet()){
            indent(shft); System.out.println(k+" "+hm.get(k));
        }
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"kind_id",hm.get("KIND_ID"));
        appendToElement(root,"name_kz",hm.get("NAME_KZ"));
        appendToElement(root,"name_ru",hm.get("NAME_RU"));
        appendToElement(root,"code",hm.get("CODE"));
        appendToElement(root,"report_period_duration_months", hm.get("REPORT_PERIOD_DURATION_MONTHS"));

    }
}
