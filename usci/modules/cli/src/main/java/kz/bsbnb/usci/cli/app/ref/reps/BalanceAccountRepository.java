package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.BalanceAccount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BalanceAccountRepository extends BaseRepository {
    //private static HashMap repository;
    //private static HashSet columns;
    //private static String QUERY = "SELECT * FROM ref.balance_account t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
    //        "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    //private static String QUERY_ALL = "SELECT * FROM ref.balance_account";
    //private static String QUERY_CLOSE = "SELECT * FROM ref.balance_account where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
    //private static String QUERY_OPEN = "SELECT * FROM ref.balance_account where open_date = to_date('repDate', 'dd.MM.yyyy') " +
    //        " and (close_date > to_date('repDate','dd.MM.yyyy' or close_date is null)";

    public BalanceAccountRepository(){
        QUERY_ALL = "SELECT * FROM ref.balance_account";
        QUERY_CLOSE = "SELECT * FROM ref.balance_account where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        QUERY_OPEN = "SELECT * FROM ref.balance_account where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BALANCE_ACCOUNT'";
    }

    @Override
    public void constructByCloseDate() {
        repository = construct(QUERY_CLOSE);
    }

    /*public static HashMap getRepository() {
        if(repository==null)
            repository = construct(QUERY_ALL);
        return repository;
    }*/

    /*public static void constructByOpenDate(){
        repository = construct(QUERY_OPEN);
    }

    public static void constructByCloseDate(){
        repository = construct(QUERY_CLOSE);
    }

    public static void constructAll(){
        repository = construct(QUERY_ALL);
    }

    public static HashMap getRepositoryByOpenDate(){
        if(repository == null)
            repository = construct(QUERY_OPEN);
        return repository;
    }*/

    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    try {
                        tmp.put((String) s, rows.getString((String) s));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                BalanceAccount dt = new BalanceAccount(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public BalanceAccount[] getByProperty(String key,String value){
        BalanceAccount [] ret = new BalanceAccount[0];
        List<BalanceAccount> list = new ArrayList<BalanceAccount>();
        for(Object v: getRepository().values()){
            if(((BalanceAccount) v).get(key) != null)
                if(((BalanceAccount) v).get(key).equals(value)) list.add((BalanceAccount)v);
        }
        return list.toArray(ret);
    }


    public BalanceAccount getById(String id){
        return (BalanceAccount) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

