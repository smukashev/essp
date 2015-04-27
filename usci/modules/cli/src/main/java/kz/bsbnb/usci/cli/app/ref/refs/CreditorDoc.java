package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public class CreditorDoc extends BaseRef {

    public CreditorDoc(HashMap hm){
        super(hm);
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"no",hm.get("NO_"));


        Element docType = getDocument().createElement("doc_type");
        root.appendChild(docType);

        DocType dt = (DocType) hm.get("doc_type");
        dt.buildElement(docType);

    }

    @Override
    public String asXml(int cnt) {
        currentXML = new StringBuilder();
        appendToXml("<no_>","</no_>",(String)hm.get("NO_"),cnt);
        appendToXml2("<doc_type>", "</doc_type>", ((DocType) hm.get("doc_type")).asXml(cnt + 1), cnt);
        return currentXML.toString();
    }

    public void print(int shft) {
        for (Object k : hm.keySet()){
            if(((String)k).equals("doc_type")){
                ((DocType) hm.get(k)).print(shft+1);
            }else{
                indent(shft); System.out.println(k+" "+hm.get(k));
            }
        }
    }
}
