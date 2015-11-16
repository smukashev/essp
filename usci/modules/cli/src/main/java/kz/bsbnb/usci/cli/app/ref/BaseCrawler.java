package kz.bsbnb.usci.cli.app.ref;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.SQLException;
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
    public static String fileName;
    public static String prefix = "C:\\refs\\";

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

            String[] dates = BaseRepository.getDatesAsStringArray(this);
            String[] closeDates = BaseRepository.getCloseDatesAsStringArray(this);
            BaseRepository.targetClass = this.getClassName();

            for(String date: dates) {

                BaseRepository.repDate = date;
                new BaseRepository().dropCache();
                BaseCrawler.fileName = BaseCrawler.prefix + date + "/";

                if (getRepository().size() < 1)
                    continue;

                File f = new File(BaseCrawler.fileName);
                f.mkdir();

                rootElement = getDocument().createElement("batch");
                getDocument().appendChild(rootElement);


                Element entities = getDocument().createElement("entities");
                rootElement.appendChild(entities);

                buildElement(entities);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(getDocument());
                StreamResult result = new StreamResult(new File(fileName + getClassName() +
                        "_" + BaseRepository.repDate.replaceAll("\\.", "_") + ".xml"));

                System.out.println("entity read  " + fileName + getClassName() +
                        "_" + BaseRepository.repDate.replaceAll("\\.", "_") + ".xml " + BaseRepository.repDate);

                //Output to console for testing
                //StreamResult result = new StreamResult(System.out);

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

                document = null;

                if(f.list().length < 1)
                    f.delete();
            }

            if("ref_creditor".equals(this.getClassName()))
                return;
            //generation of closed refs
            BaseRepository.enterClosedMode(this);

            for(String date: closeDates) {
                BaseRepository.repDate = date;
                new BaseRepository().dropCache();
                BaseCrawler.fileName = BaseCrawler.prefix + date + "/";
                if (getRepository().size() < 1)
                    continue;

                File f = new File(BaseCrawler.fileName);
                f.mkdir();

                rootElement = getDocument().createElement("batch");
                getDocument().appendChild(rootElement);

                Element entities = getDocument().createElement("entities");
                rootElement.appendChild(entities);

                buildElementForClose(entities);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(getDocument());
                StreamResult result = new StreamResult(new File(fileName + getClassName() +
                        "_c_" + BaseRepository.repDate.replaceAll("\\.", "_") + ".xml"));

                System.out.println("entity read  " + fileName + getClassName() +
                        "_c_" + BaseRepository.repDate.replaceAll("\\.", "_") + ".xml " + BaseRepository.repDate);

                //Output to console for testing
                //StreamResult result = new StreamResult(System.out);

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(source, result);

                document = null;

                if(f.listFiles().length < 1)
                    f.delete();
            }

            BaseRepository.exitClosedMode();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void buildElement(Element root){

        //int i = 0;
        for( Object o: getRepository().values()){

            //if(i>5) break;
            //BaseRef creditor = (BaseRef) getRef().cast(o);
            BaseRef creditor = (BaseRef) o;

            Element entityClass = getDocument().createElement(getClassName());
            root.appendChild(entityClass);

            creditor.buildElement(entityClass);

             /*appendToElement(entity,"name",creditor.get("NAME"));
             appendToElement(entity,"short_name",creditor.get("SHORT_NAME"));*/

            //i++;
        }

    }

    public void buildElementForClose(Element root){

        //int i = 0;
        for( Object o: getRepository().values()){

            //if(i>5) break;
            //BaseRef creditor = (BaseRef) getRef().cast(o);
            BaseRef creditor = (BaseRef) o;

            Element entityClass = getDocument().createElement(getClassName());
            entityClass.setAttribute("operation","CLOSE");

            root.appendChild(entityClass);

            creditor.buildElement(entityClass);

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
        throw new NotImplementedException();
    }

    public HashMap getRepository(){
        throw new NotImplementedException();
    }

    public Class  getRef(){
        throw new NotImplementedException();
    }
}
