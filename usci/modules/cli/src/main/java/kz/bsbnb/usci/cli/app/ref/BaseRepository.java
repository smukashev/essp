package kz.bsbnb.usci.cli.app.ref;

import kz.bsbnb.usci.cli.app.ref.craw.*;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorDoc;
import kz.bsbnb.usci.cli.app.ref.refs.DocType;
import kz.bsbnb.usci.cli.app.ref.reps.*;
import kz.bsbnb.usci.eav.util.DataUtils;
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
import java.util.*;

/**
 * My BaseRepository
 *
 */
public class BaseRepository implements  Runnable
{
     private final String startDate = "01.01.2012";
     private final String endDate = "01.01.2012";

     private static Connection connection;
     private static Statement statement;
     protected static String repDate;
    public static boolean closeMode;
    public static String QUERY;
    public static String targetClass;

    @Override
    public void run() {

        guaranteeNotTooLong(startDate, endDate);

        String exclusiveEndDate = getNextRepDate(endDate);
        String curDate = startDate;
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
            /*(new CreditorBranchCrawler()).work();
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
            /*new BACTCrawler().work();
            new DRTCrawler().work();
            new BADRTCrawler().work();*/

            if(f.list().length == 0) {
                f.delete();
            }

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
        int year = (startDate.charAt(6) - '0') * 1000 + (startDate.charAt(7) - '0') * 100
                + (startDate.charAt(8) - '0') * 10 + (startDate.charAt(9) - '0');

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
        int year =   (date.charAt(6) - '0') * 1000 + (date.charAt(7) - '0') * 100
                + (date.charAt(8) - '0') * 10 + (date.charAt(9) - '0');
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
                    connection = DriverManager.getConnection("jdbc:oracle:thin:@10.10.20.44:1521:CREDITS", "core","core_sep_2014");
                    return statement = connection.createStatement();
            }

           if(statement == null)
             statement = connection.createStatement();

           return statement;
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public static String resolveTable(BaseCrawler crawler){
        switch (crawler.getClassName()) {
            case "ref_creditor":
                return "ref.v_creditor_his";
            default:
                return crawler.getClassName().replaceAll("ref_", "ref.");
        }
    }

    public static String resolveWhereForClosedDate(BaseCrawler crawler){
        switch (crawler.getClassName()) {
            case "ref_creditor":
                return " where close_date is not null and main_office_id is null";
            default:
                return " where close_date is not null and is_last = 1";
        }

    }

    public static String[] getDatesAsStringArray(BaseCrawler crawler) throws SQLException {
        ResultSet rows = getStatement()
                .executeQuery("select distinct(to_char(open_date,'dd.MM.yyyy')) as open_date from "
                        + resolveTable(crawler));
        List<String> ret = new LinkedList<>();

        while(rows.next()){
            ret.add(rows.getString("open_date"));
        }

        return ret.toArray(new String[0]);
    }

    public static String[] getCloseDatesAsStringArray(BaseCrawler crawler) throws SQLException {
        ResultSet rows = getStatement()
                .executeQuery("select distinct(to_char(close_date,'dd.MM.yyyy')) as close_date from "
                        + resolveTable(crawler) + resolveWhereForClosedDate(crawler));
        List<String> ret = new LinkedList<String>();

        while(rows.next()){
            if(rows.getString("close_date") != null)
                ret.add(rows.getString("close_date"));
        }

        return ret.toArray(new String[0]);
    }

    public static void enterClosedMode(BaseCrawler crawler){
        closeMode = true;
        QUERY = "select * from " + resolveTable(crawler) + resolveWhereForClosedDate(crawler);
    }

    public static void exitClosedMode(){
        closeMode = false;
    }
}
