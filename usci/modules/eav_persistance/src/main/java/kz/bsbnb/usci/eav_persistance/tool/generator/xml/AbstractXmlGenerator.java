package kz.bsbnb.usci.eav_persistance.tool.generator.xml;

import org.w3c.dom.Document;

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
import java.io.IOException;

/**
 * @author k.tulbassiyev
 */
public abstract class AbstractXmlGenerator
{
    public Document getDocument()
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;

        try
        {
            documentBuilder= documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }

        return documentBuilder.newDocument();
    }

    public void writeToXml(Document document, String filePath)
    {
        File xmlFile = new File(filePath);

        if(!xmlFile.exists())
        {
            try
            {
                xmlFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;

        try
        {
            transformer = transformerFactory.newTransformer();
        }
        catch (TransformerConfigurationException e)
        {
            e.printStackTrace();
        }

        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(new File(filePath));

        try
        {
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
    }
}
