package kz.bsbnb.usci.cli.app.ref;

import kz.bsbnb.usci.cli.app.ref.craw.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.Subject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * My BaseRepository
 *
 */
public class BaseRepository implements  Runnable
{

      private static Connection connection;
      private static Statement statement;

    private String fileName = "C:\\entity_show\\mine";

    public void saveXml(String[] lookup, ResultSet rows, String path){
         try {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

             Document doc = docBuilder.newDocument();
             Element rootElement = doc.createElement("batch");
             doc.appendChild(rootElement);

             Element entities = doc.createElement("entities");
             rootElement.appendChild(entities);

             while(rows.next()){
                 Element entity = doc.createElement("entity");
                 for(int i=0;i<lookup.length;i++){
                     Element ne = doc.createElement(lookup[i]);
                     String val = rows.getString(lookup[i]);
                     ne.appendChild(doc.createTextNode(val==null?" ":val));
                     entity.appendChild(ne);
                 }
                 entities.appendChild(entity);

             }

             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             StreamResult result = new StreamResult(new File(path));

             // Output to console for testing
             // StreamResult result = new StreamResult(System.out);

             transformer.transform(source, result);


         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }

    @Override
    public void run() {

        (new CreditorCrawler()).work();
        (new SubjectTypeCrawler()).work();
        (new CreditorDocCrawler()).work();
        (new BalanceAccountCrawler()).work();
        (new BankRelationCrawler()).work();
        (new ClassificationCrawler()).work();
        (new CountryCrawler()).work();
        (new CreditObjectCrawler()).work();
        (new CreditPurposeCrawler()).work();
        (new CurrencyCrawler()).work();
        (new EconTradeCrawler()).work();
        (new EnterpriseTypeCrawler()).work();
        (new FinanceSourceCrawler()).work();
        (new LegalFormCrawler()).work();
        (new OffshoreCrawler()).work();
        (new PledgeTypeCrawler()).work();
        (new OffshoreCrawler()).work();
        (new PledgeTypeCrawler()).work();
        (new PortfolioCrawler()).work();
        (new RegionCrawler()).work();
        (new ContactTypeCrawler()).work();
        (new CreditTypeCrawler()).work();
        (new DocTypeCrawler()).work();
        (new SharedCrawler()).work();
        (new CreditorBranchCrawler()).work();
        (new NokbdbCrawler()).work();


    }

//    public static void main( String[] args )
//    {
//        new BaseRepository().run();
//    }

    public static Statement getStatement(){
        try {
            if(connection == null){
                ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
                DriverManagerDataSource dm = (DriverManagerDataSource)ctx.getBean("dataSourceRef");
                connection = dm.getConnection();
                return statement = connection.createStatement();
            }

            return statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
