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
     private final String startDate = "01.12.2014";
     private final String endDate = "01.12.2014";

     private static Connection connection;
     private static Statement statement;
     protected static String repDate;

    @Override
    public void run() {

        guaranteeNotTooLong(startDate, endDate);

        String exclusiveEndDate = getNextRepDate(endDate);

        int mo = (startDate.charAt(3) - '0' )* 10 + (startDate.charAt(4) - '0');
        int year = 2000 + (startDate.charAt(8) - '0') * 10 + (startDate.charAt(9) - '0');
        String curDate = startDate;

        boolean oneMore = false;

        while(true){
            dropCache();
            BaseRepository.repDate = curDate;

            BaseCrawler.fileName = BaseCrawler.prefix + curDate + "/";
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

            curDate = getNextRepDate(curDate);
            if(curDate.equals(exclusiveEndDate))
                break;
        }

        System.out.println("Done.");
    }

    public void dropCache(){
        BACTRepository.rc();
        BADRTRepository.rc();
        BalanceAccountRepository.rc();
        BankRelationRepository.rc();
        ClassificationRepository.rc();
        ContactTypeRepository.rc();
        CountryRepository.rc();
        CreditObjectRepository.rc();
        CreditorBranchRepository.rc();
        CreditorDocRepository.rc();
        CreditorRepository.rc();
        CreditPurposeRepository.rc();
        CreditTypeRepository.rc();
        CurrencyRepository.rc();
        DebtorTypeRepository.rc();
        DocTypeRepository.rc();
        DRTRepository.rc();
        EconSectorRepository.rc();
        EconTradeRepository.rc();
        EnterpriseTypeRepository.rc();
        FinanceSourceRepository.rc();
        LegalFormRepository.rc();
        MetaRepository.rc();
        NokbdbRepository.rc();
        OffshoreRepository.rc();
        PledgeTypeRepository.rc();
        PortfolioRepository.rc();
        RegionRepository.rc();
        SharedRepository.rc();
        SubjectTypeRepository.rc();
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

    public String getNextRepDate(String date){
        int mo = (date.charAt(3) - '0' )* 10 + (date.charAt(4) - '0');
        int year = 2000 + (date.charAt(8) - '0') * 10 + (date.charAt(9) - '0');
        mo = mo + 1 < 13 ? mo + 1 : 1;
        if(mo == 1) year ++;

        return "01." + mo / 10 + "" + mo % 10 + "." + year;
    }

    public static void main( String[] args )
    {
        new BaseRepository().run();
    }

    public static Statement getStatement(){
        try {
            if(connection == null){
                    connection = DriverManager.getConnection("jdbc:oracle:thin:@170.7.15.97:1521:CREDITS", "core","core_aug_2015");
                    return statement = connection.createStatement();
            }

            return statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }
}
