package kz.bsbnb.usci.cli.app.ref.refs;


import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */
public class DocType extends BaseRef {

    private HashMap hm;

    public DocType(HashMap hm){
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
        appendToElement(root,"name_ru", hm.get("NAME_RU"));
        appendToElement(root, "name_kz",hm.get("NAME_KZ"));
        appendToElement(root, "is_identification",(String)hm.get("IS_IDENTIFICATION"));
        appendToElement(root,"is_organization_doc",hm.get("IS_ORGANIZATION_DOC"));
        appendToElement(root,"is_person_doc",hm.get("IS_PERSON_DOC"));
        appendToElement(root,"weight",hm.get("WEIGHT"));
        appendToElement(root,"code",hm.get("CODE"));
    }

    @Override
    public String asXml(int cnt) {
        currentXML = new StringBuilder();
        appendToXml("<name_ru>","</name_ru>",(String)hm.get("NAME_RU"),cnt);
        appendToXml("<name_kz>","</name_kz>",(String)hm.get("NAME_KZ"),cnt);
        appendToXml("<is_identification>","</is_identification>",(String)hm.get("IS_IDENTIFICATION"),cnt);
        appendToXml("<is_organization_doc>","</is_organization_doc>",(String)hm.get("IS_ORGANIZATION_DOC"),cnt);
        appendToXml("<is_person_doc>","</is_person_doc>",(String)hm.get("IS_PERSON_DOC"),cnt);
        appendToXml("<weight>","</weight>",(String)hm.get("WEIGHT"),cnt);
        return currentXML.toString();
    }

    public void print(int shft) {
        for (Object k : hm.keySet()){
            indent(shft); System.out.println(k+" "+hm.get(k));
        }
    }
}
