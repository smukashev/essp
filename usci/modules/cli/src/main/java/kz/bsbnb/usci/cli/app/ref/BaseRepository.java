package kz.bsbnb.usci.cli.app.ref;

import kz.bsbnb.usci.cli.app.ref.craw.*;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorDoc;
import kz.bsbnb.usci.cli.app.ref.refs.DocType;
import kz.bsbnb.usci.cli.app.ref.reps.*;
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
      protected static String repDate = "01.03.2014";

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
        //DocTypeRepository.getById("1043").print(0);
        //SubjectTypeRepository.getById("8").print();
        //CreditorRepository.getById("127").print(0);
        //CreditorDocRepository.getById("118").print();
        //BalanceAccountRepository.getById("322").

        //CreditorDoc[] arr = CreditorDocRepository.getByProperty("CREDITOR_ID","126");

            /*for(int i=0;i<arr.length;i++)
            {
                arr[i].print();
                System.out.println("------------");
            } */

            /*for(int i=103;i<=127;i++)
                if(CreditorRepository.getById(i+"")!=null)
                    System.out.print(CreditorRepository.getById(i+"").asXml(0));
             */


        (new CreditorCrawler()).work();
        (new SubjectTypeCrawler()).work();
        (new CreditorDocCrawler()).work();
        (new BalanceAccountCrawler()).work();
        (new BankRelationCrawler()).work();
        (new ClassificationCrawler()).work();
        (new CountryCrawler()).work();
        (new CreditObjectCrawler()).work();
        (new CreditPurposeCrawler()).work();  //here
        (new CurrencyCrawler()).work();
        (new EconTradeCrawler()).work();
        (new EnterpriseTypeCrawler()).work();
        (new FinanceSourceCrawler()).work();
        (new LegalFormCrawler()).work();
        (new OffshoreCrawler()).work();
        (new PledgeTypeCrawler()).work();
        (new PortfolioCrawler()).work();
        (new RegionCrawler()).work();
        (new ContactTypeCrawler()).work();
        (new CreditTypeCrawler()).work();
        (new DocTypeCrawler()).work();
        //(new SharedCrawler()).work();
        (new CreditorBranchCrawler()).work();
        (new NokbdbCrawler()).work();
        (new EconSectorCrawler()).work();
    }

    public static void main( String[] args )
    {
        new BaseRepository().run();
    }

    public static Statement getStatement(){
        try {
            if(connection == null){
                    connection = DriverManager.getConnection("jdbc:oracle:thin:@10.10.20.44:1521:CREDITS", "core","core_sep_2014");
                    return statement = connection.createStatement();
            }

            return statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }
}
