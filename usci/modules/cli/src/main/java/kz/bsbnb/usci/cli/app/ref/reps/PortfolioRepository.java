
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.CreditorCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Portfolio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PortfolioRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.PORTFOLIO t where " +
                        " t.open_date <= to_date('repDate', 'dd.MM.yyyy')\n"+
                        " and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='PORTFOLIO'";*/

    public PortfolioRepository() {
        QUERY_ALL = "SELECT * FROM ref.portfolio";
        QUERY_OPEN = "SELECT * FROM ref.portfolio where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null)";
        QUERY_CLOSE = "SELECT * FROM ref.portfolio where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='PORTFOLIO'";
        creditorCrawler = new CreditorCrawler();
        creditorCrawler.constructAll();
    }

    CreditorCrawler creditorCrawler;

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            //CreditorRepository.getRepository();
            CreditorRepository creditorRepository = (CreditorRepository) creditorCrawler.getRepositoryInstance();
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));
            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }

                tmp.put("creditor", creditorRepository.getById((String)tmp.get("CREDITOR_ID")));
                Portfolio dt = new Portfolio(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Portfolio[] getByProperty(String key,String value){
        Portfolio [] ret = new Portfolio[0];
        List<Portfolio> list = new ArrayList<Portfolio>();
        for(Object v: getRepository().values()){
            if(((Portfolio) v).get(key) != null)
                if(((Portfolio) v).get(key).equals(value)) list.add((Portfolio)v);
        }
        return list.toArray(ret);
    }

    public Portfolio getById(String id){
        return (Portfolio) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

