package kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.helper;

import kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.data.AttributeTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * @author abukabayev
 */
public class TreeGenerator {

    private AttributeTree getChildren(NodeList list,AttributeTree tree){
        for (int i=0;i<list.getLength();i++){
            Node node = list.item(i);
            String name = node.getNodeName();

            if (!name.equals("#text")){
                getChildren(node.getChildNodes(),tree.addChild(new AttributeTree(name,tree)));
            }
        }

        return tree;
    }

    public AttributeTree generateTree(AttributeTree tree) throws ParserConfigurationException {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        try {

            Document doc = docBuilder.parse(new File("usci/modules/eav_persistance/src/main/resources/generate.xml"));
            doc.getDocumentElement().normalize();
            NodeList list = doc.getChildNodes();

            tree = getChildren(list,tree);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tree;
    }
}