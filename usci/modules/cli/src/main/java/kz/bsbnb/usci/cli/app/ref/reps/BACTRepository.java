package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.BalanceAccountCrawler;
import kz.bsbnb.usci.cli.app.ref.craw.CreditTypeCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.BACT;
import kz.bsbnb.usci.cli.app.ref.refs.BalanceAccount;
import kz.bsbnb.usci.cli.app.ref.refs.CreditType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Bauyrzhan.Makhambeto on 12/06/2015.
 */
public class BACTRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT * FROM ref.ba_ct t" + " where t.open_date <= to_date('repDate', 'dd.MM.yyyy') \n"+
   //         "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";

    private static String QUERY = "select t1.id, t1.balance_account_id, t1.credit_type_id, t2.open_date, t2.close_date  from ref.ba_ct t1, ref.balance_account t2 \n" +
            "       where t1.balance_account_id = t2.id        \n" +
            "       and t1.open_date = to_date ('repDate','dd.MM.yyyy')\n" +
            "       and (t1.close_date > to_date ('repDate','dd.MM.yyyy') or t1.close_date is null)\n" +
            "       and t2.open_date <= to_date ('repDate','dd.MM.yyyy')\n" +
            "       and (t2.close_date > to_date ('repDate','dd.MM.yyyy') or t2.close_date is null)\n" +
            "       and t1.balance_account_id = t2.id\n";


    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BA_CT'";*/

    public BACTRepository() {
        QUERY_ALL = "SELECT * FROM ref.ba_ct";
        QUERY_OPEN = "SELECT * FROM ref.ba_ct t where t.open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (t.close_date > to_date('repDate','dd.MM.yyyy') or t.close_date is null)" +
                " and exists(select 1 from ref.balance_account where id = t.balance_account_id)";
        QUERY_CLOSE = "SELECT * FROM ref.ba_ct where close_date = to_date('repDate', 'dd.MM.yyyy') and open_date < close_date";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BA_CT'";

        balanceAccountCrawler = new BalanceAccountCrawler();
        balanceAccountCrawler.constructAll();
        creditTypeCrawler = new CreditTypeCrawler();
        creditTypeCrawler.constructAll();
    }

    BalanceAccountCrawler balanceAccountCrawler;
    CreditTypeCrawler creditTypeCrawler;


    @Override
    public HashMap construct(String query){
        try {
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();

                BalanceAccountRepository balanceAccountRepository = (BalanceAccountRepository)balanceAccountCrawler.getRepositoryInstance();

                BalanceAccount ba = balanceAccountRepository.getById((String) rows.getString("BALANCE_ACCOUNT_ID"));
                tmp.put("balance_account",ba);

                CreditTypeRepository creditTypeRepository = (CreditTypeRepository)creditTypeCrawler.getRepositoryInstance();

                CreditType ct = creditTypeRepository.getById((String)rows.getString("CREDIT_TYPE_ID"));
                tmp.put("credit_type", ct);
                BACT bact = new BACT(tmp);

                tmp.put("ID", rows.getString("ID"));

                hm.put(bact.get(bact.getKeyName()),bact);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BACT getById(String id){
        return (BACT) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}
