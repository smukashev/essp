package kz.bsbnb.usci.eav.tool.generator.nonrandom.xml;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public void writeToZip(Document[] documents, String[] names, String path){

        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            out.setMethod(ZipOutputStream.DEFLATED);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        byte data[] = new byte[2048];
        for(int i=0;i<documents.length;i++){
            ZipEntry entry = new ZipEntry(names[i]);
            try {
                out.putNextEntry(entry);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Source xmlSource = new DOMSource(documents[i]);
                Result outputTarget = new StreamResult(outputStream);
                TransformerFactory.newInstance().newTransformer().transform(xmlSource,outputTarget);
                InputStream origin = new ByteArrayInputStream(outputStream.toByteArray());
                int count = 0;
                while((count = origin.read(data, 0, 2048)) != -1) {
                    out.write(data, 0, count);
                }

                origin.close();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
                return;
            } catch (TransformerException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}