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
     private final String startDate = "01.04.2013";
     private final String endDate = "01.05.2015";

     private static Connection connection;
     private static Statement statement;
     protected static String repDate = "01.02.2015";

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

        guaranteeNotTooLong(startDate, endDate);

        int mo = (startDate.charAt(3) - '0' )* 10 + (startDate.charAt(4) - '0');
        int year = 2000 + (startDate.charAt(8) - '0') * 10 + (startDate.charAt(9) - '0');
        String curDate = startDate;

        boolean oneMore = false;

        while(true){

            BaseRepository.repDate = curDate;

            BaseCrawler.fileName = BaseCrawler.prefix + curDate + "\\";
            File f = new File(BaseCrawler.fileName);
            f.mkdir();

            (new SubjectTypeCrawler()).work();
            (new DocTypeCrawler()).work();
            (new BalanceAccountCrawler()).work();
            (new BankRelationCrawler()).work();
            (new ClassificationCrawler()).work();
            (new ContactTypeCrawler()).work();
            (new CountryCrawler()).work();
            (new CreditObjectCrawler()).work();
            (new CreditPurposeCrawler()).work();
            (new CreditTypeCrawler()).work();
            (new RegionCrawler()).work();
            //(new CreditorDocCrawler()).work(); //obsolete
            (new CreditorCrawler()).work();
            (new CreditorBranchCrawler()).work();
            (new CurrencyCrawler()).work();
            (new EconTradeCrawler()).work();
            (new EnterpriseTypeCrawler()).work();
            (new FinanceSourceCrawler()).work();
            (new LegalFormCrawler()).work();
            (new OffshoreCrawler()).work();
            (new PledgeTypeCrawler()).work();
            (new PortfolioCrawler()).work();

            //(new SharedCrawler()).work(); //not used
            //(new NokbdbCrawler()).work(); //not used
            //(new EconSectorCrawler()).work(); //not used
            new BACTCrawler().work();
            new DRTCrawler().work();
            new BADRTCrawler().work();

            mo ++;
            if(mo == 13) {
                mo = 1;
                year ++;
            }

            curDate = "01." + mo / 10 + "" + mo % 10 + "." + year;

            if(oneMore)
                break;

            oneMore = curDate.equals(endDate);

        }
    }

    public void guaranteeNotTooLong(String startDate, String endDate){
        if(startDate.equals(endDate))
            return;
        if(!startDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"))
            throw new RuntimeException("start format not correct");

        if(!endDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"))
            throw new RuntimeException("end format not correct");

        int mo = (startDate.charAt(3) - '0' )* 10 + (startDate.charAt(4) - '0');
        int year = 2000 + (startDate.charAt(8) - '0') * 10 + (startDate.charAt(9) - '0');

        int cnt = 0;

        while(cnt < 300) {
            mo ++;
            cnt++;
            if(mo == 13) {
                mo = 1;
                year ++;
            }

            String prob = "01." + mo / 10 + "" + mo % 10 + "." + year;
            if(prob.equals(endDate))
                return;
        }

        throw new RuntimeException("too long period given");
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
