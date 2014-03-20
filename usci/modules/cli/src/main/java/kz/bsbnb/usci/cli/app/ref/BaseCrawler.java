package kz.bsbnb.usci.cli.app.ref;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
public class BaseCrawler {


    public static Document document;
    public static String fileName = "C:\\entity_show\\test";
    public static Document getDocument(){

        if(document==null){
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                return document = docBuilder.newDocument();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

        return document;

    }

    public Element rootElement;

    public void work(){

        try {
            rootElement = getDocument().createElement("batch");
            getDocument().appendChild(rootElement);


            Element entities = getDocument().createElement("entities");
            rootElement.appendChild(entities);

            buildElement(entities);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(getDocument());
            StreamResult result = new StreamResult(new File(fileName+"\\" + getClassName()+".xml"));

            transformer.transform(source, result);

            document = null;


        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void buildElement(Element root){

        //int i = 0;
        for( Object o: getRepository().values()){

            //if(i>5) break;
            //BaseRef creditor = (BaseRef) getRef().cast(o);
            BaseRef creditor = (BaseRef) o;

            Element entity = getDocument().createElement("entity");
            root.appendChild(entity);

            Attr attr = getDocument().createAttribute("class");
            attr.setValue(getClassName());
            entity.setAttributeNode(attr);

            creditor.buildElement(entity);

             /*appendToElement(entity,"name",creditor.get("NAME"));
             appendToElement(entity,"short_name",creditor.get("SHORT_NAME"));*/

            //i++;
        }

    }


    /**
     * Simply adds new tag to ther parent tag
     *
     * @param element to what will be added
     * @param name what tag will be added
     * @param value text of adding tag
     */
    public void appendToElement(Element element,String name,String value){
        Element newElement = getDocument().createElement(name);
        newElement.appendChild(getDocument().createTextNode(value));
        element.appendChild(newElement);
    }


    public String getClassName(){
        throw new RuntimeException("not implemented");
    }

    public HashMap getRepository(){
        throw new RuntimeException("not implemented");
    }

    public Class  getRef(){
        throw new RuntimeException("not implemented");
    }
}
