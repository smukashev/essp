package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.BankRelation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BankRelationRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT * FROM ref.BANK_RELATION t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
    //        "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    //private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BANK_RELATION'";

    public BankRelationRepository() {
        QUERY_ALL = "SELECT * FROM ref.bank_relation";
        QUERY_OPEN = "SELECT * FROM ref.bank_relation where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.%s where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='BANK_RELATION'";
    }


    @Override
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
                    tmp.put((String)s,rows.getString((String)s));
                }
                BankRelation dt = new BankRelation(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BankRelation[] getByProperty(String key,String value){
        BankRelation [] ret = new BankRelation[0];
        List<BankRelation> list = new ArrayList<BankRelation>();
        for(Object v: getRepository().values()){
            if(((BankRelation) v).get(key) != null)
                if(((BankRelation) v).get(key).equals(value)) list.add((BankRelation)v);
        }
        return list.toArray(ret);
    }

    public BankRelation getById(String id){
        return (BankRelation) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

