package kz.bsbnb.usci.cli.app.ref;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
public class BaseRef {
    protected HashMap hm;

    public String get(String s){
        return (String) hm.get(s);
    }

    public String getKeyName(){
        return "ID";
    }

    public BaseRef(HashMap hm){
        this.hm = hm;
    }

    public void indent(int shft){
        for(int i=0;i<shft;i++)
            System.out.print("  ");
    }

    public String indend(int shft){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<shft;i++)
            sb.append("  ");
        return sb.toString();
    }


    public boolean debug = true;
    public StringBuilder currentXML = new StringBuilder();

    public void appendToXml(String openTag,String closeTag,String value,int shft){
        currentXML.append(indend(shft));
        currentXML.append(openTag);
        currentXML.append(value);
        currentXML.append(closeTag);
        currentXML.append("\n");
    }

    public void appendToXml2(String openTag,String closeTag,String value,int shft){
        currentXML.append(indend(shft));
        currentXML.append(openTag);
        currentXML.append("\n");
        currentXML.append(value);
        currentXML.append(indend(shft));
        currentXML.append(closeTag);
        currentXML.append("\n");
    }

    public String asXml(int cnt){
        return "";
    }

    public void buildElement(Element root){
        throw new NotImplementedException();
    }

    public void appendToElement(Element element,String name,String value){
        if(value == null) return;
        Element newElement = getDocument().createElement(name);
        newElement.appendChild(getDocument().createTextNode(value));
        element.appendChild(newElement);
    }

    public void appendToElement(Element element,String name,Object value){
        appendToElement(element,name,(String) value);
    }

    public  Document getDocument(){
        return BaseCrawler.getDocument();
    }
}
