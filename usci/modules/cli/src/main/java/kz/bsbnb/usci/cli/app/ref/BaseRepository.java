package kz.bsbnb.usci.cli.app.ref;

import kz.bsbnb.usci.cli.app.ref.craw.*;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
    //public static boolean closeMode;

    protected HashMap repository;
    protected HashSet columns;

    //public static String QUERY;
    //public static String targetClass;

    @Override
    public void run() {

        guaranteeNotTooLong(startDate, endDate);

        String exclusiveEndDate = getNextRepDate(endDate);
        String curDate = startDate;
        while(true){
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
            new ExclDocCrawler().work();

            if(f.list().length == 0) {
                f.delete();
            }

            curDate = getNextRepDate(curDate);
            if(curDate.equals(exclusiveEndDate))
                break;
        }

        System.out.println("Done.");
    }

    public void guaranteeNotTooLong(String startDate, String endDate){
        if(startDate.equals(endDate))
            return;
        if(!startDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"))
            throw new RuntimeException(Errors.compose(Errors.E224));

        if(!endDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}"))
            throw new RuntimeException(Errors.compose(Errors.E225));

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

        throw new RuntimeException(Errors.compose(Errors.E226));
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
                    connection = DriverManager.getConnection("jdbc:oracle:thin:@" + StaticRouter.getCRDBIP() +
                            ":1521:CREDITS", StaticRouter.getCRDBUsername(), StaticRouter.getCRDBPassword());
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
            case "ref_creditor_branch":
                return "ref.v_creditor_his";
            case "ref_debt_remains_type":
                return "dual";
            case "ref_exclusive_doc":
                return "REF.SPECIAL_DOC_NO";
            default:
                return crawler.getClassName().replaceAll("ref_", "ref.");
        }
    }

    public static String resolveWhereForOpenDate(BaseCrawler crawler){
        switch (crawler.getClassName()) {
            case "ref_creditor":
                return " where main_office_id is null and open_date is not null";
            default:
                return " where open_date is not null";
        }
    }

    public static String resolveWhereForClosedDate(BaseCrawler crawler){
        switch (crawler.getClassName()) {
            case "ref_creditor":
                return " where shutdown_date is not null and main_office_id is null";
            case "ref_creditor_branch":
                return " where close_date is not null and main_office_id is not null";
            case "ref_ba_ct":
                return " where close_date is not null";
            default:
                return " where close_date is not null and is_last = 1";
        }
    }

    public static String[] getDatesAsStringArray(BaseCrawler crawler) throws SQLException {

        //because it is from table shared
        if(crawler instanceof DRTCrawler || crawler instanceof ExclDocCrawler)
            return new String[] {"01.01.1990"};

        try {
            ResultSet rows = getStatement()
                    .executeQuery("select to_char(open_date,'dd.MM.yyyy') as open_date from (select distinct open_date from "
                            + resolveTable(crawler) + resolveWhereForOpenDate(crawler) + " order by open_date)");
            List<String> ret = new LinkedList<>();

            while (rows.next()) {
                ret.add(rows.getString("open_date"));
            }

            return ret.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String[] getCloseDatesAsStringArray(BaseCrawler crawler) throws SQLException {

        if(crawler instanceof  DRTCrawler)
            return new String[] {};

        if(crawler instanceof BADRTCrawler)
            return new String[] {};

        if(crawler instanceof ExclDocCrawler)
            return  new String[] {};

        ResultSet rows;

        if(crawler instanceof CreditorCrawler) {
            rows = getStatement()
                    .executeQuery("select distinct(to_char(shutdown_date,'dd.MM.yyyy')) as close_date from "
                            + resolveTable(crawler) + resolveWhereForClosedDate(crawler));
        } else {
            rows = getStatement()
                    .executeQuery("select distinct(to_char(close_date,'dd.MM.yyyy')) as close_date from "
                            + resolveTable(crawler) + resolveWhereForClosedDate(crawler));
        }
        List<String> ret = new LinkedList<>();

        while(rows.next()){
            if(rows.getString("close_date") != null)
                ret.add(rows.getString("close_date"));
        }

        return ret.toArray(new String[0]);
    }

    protected String QUERY_ALL;
    protected String QUERY_CLOSE;
    protected String QUERY_OPEN;
    protected String COLUMNS_QUERY;

    public void constructByCloseDate(){
        repository = construct(QUERY_CLOSE);
    }

    public void constructByOpenDate(){
        repository = construct(QUERY_OPEN);
    }

    public void constructAll(){
        repository = construct(QUERY_ALL);
    }

    public HashMap construct(String query){
        throw new NotImplementedException();
    }

    public HashMap getRepository(){
        return repository;
    }

    protected HashSet getColumns() {
        try {
            if(columns ==null){
                ResultSet rows = getStatement().executeQuery(COLUMNS_QUERY);
                HashSet hs = new HashSet();
                while(rows.next()){
                    hs.add(rows.getString("column_name"));
                }
                return columns = hs;
            }
            return columns;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    protected BaseRepository getRepositoryInstance(){
        return repositoryInstance;
    }*/

    /*public static void enterClosedMode(BaseCrawler crawler){
        closeMode = true;
        QUERY = "select * from " + resolveTable(crawler) + " where close_date = to_date"
    }

    public static void exitClosedMode(){
        closeMode = false;
    }*/
}
