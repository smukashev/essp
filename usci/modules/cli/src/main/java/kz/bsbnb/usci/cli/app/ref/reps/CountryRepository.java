package kz.bsbnb.usci.cli.app.ref.reps;


import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.Country;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CountryRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.COUNTRY t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='COUNTRY'";*/

    public CountryRepository() {
        QUERY_ALL = "SELECT * FROM ref.country";
        QUERY_OPEN = "SELECT * FROM ref.country where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.%s where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='COUNTRY'";
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
                Country dt = new Country(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Country[] getByProperty(String key,String value){
        Country [] ret = new Country[0];
        List<Country> list = new ArrayList<Country>();
        for(Object v: getRepository().values()){
            if(((Country) v).get(key) != null)
                if(((Country) v).get(key).equals(value)) list.add((Country)v);
        }
        return list.toArray(ret);
    }

    public Country getById(String id){
        return (Country) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

