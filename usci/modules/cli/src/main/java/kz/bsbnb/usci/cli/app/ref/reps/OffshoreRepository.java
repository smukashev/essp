
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.Country;
import kz.bsbnb.usci.cli.app.ref.refs.Offshore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OffshoreRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.OFFSHORE t" + " where t.open_date <= to_date('" + repDate + "', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('" + repDate + "', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='OFFSHORE'";

    public static HashMap getRepository() {
        if(repository ==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
        try {
            ResultSet rows = getStatement().executeQuery(QUERY);

            HashMap hm = new HashMap();
            while(rows.next()){
                HashSet hs = getColumns();
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }

                Country country = CountryRepository.getById((String)tmp.get("COUNTRY_ID"));
                tmp.put("country",country);

                Offshore dt = new Offshore(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Offshore[] getByProperty(String key,String value){
        Offshore [] ret = new Offshore[0];
        List<Offshore> list = new ArrayList<Offshore>();
        for(Object v: getRepository().values()){
            if(((Offshore) v).get(key) != null)
                if(((Offshore) v).get(key).equals(value)) list.add((Offshore)v);
        }
        return list.toArray(ret);
    }

    public static Offshore getById(String id){
        return (Offshore) getRepository().get(id);
    }

    public static HashSet getColumns() {
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
}

