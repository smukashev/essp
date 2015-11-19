
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.CountryCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Country;
import kz.bsbnb.usci.cli.app.ref.refs.Offshore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OffshoreRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.OFFSHORE t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='OFFSHORE'";*/

    public OffshoreRepository() {
        QUERY_ALL = "SELECT * FROM ref.offshore";
        QUERY_OPEN = "SELECT * FROM ref.offshore where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.offshore where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='OFFSHORE'";
        countryCrawler = new CountryCrawler();
        countryCrawler.constructAll();
    }

    CountryCrawler countryCrawler;

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            CountryRepository countryRepository = (CountryRepository) countryCrawler.getRepositoryInstance();
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }

                Country country = countryRepository.getById((String)tmp.get("COUNTRY_ID"));
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

    public Offshore[] getByProperty(String key,String value){
        Offshore [] ret = new Offshore[0];
        List<Offshore> list = new ArrayList<Offshore>();
        for(Object v: getRepository().values()){
            if(((Offshore) v).get(key) != null)
                if(((Offshore) v).get(key).equals(value)) list.add((Offshore)v);
        }
        return list.toArray(ret);
    }

    public Offshore getById(String id){
        return (Offshore) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

