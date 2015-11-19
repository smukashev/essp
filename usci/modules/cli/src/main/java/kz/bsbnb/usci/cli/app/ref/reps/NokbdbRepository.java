package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.Nokbdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 18.12.13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class NokbdbRepository extends BaseRepository{
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.CREDITOR_NOKBDB";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR_NOKBDB'";*/

    public NokbdbRepository() {
        QUERY_ALL = "SELECT * FROM ref.CREDITOR_NOKBDB";
        QUERY_OPEN = "SELECT * FROM ref.CREDITOR_NOKBDB";
        QUERY_CLOSE = "SELECT * FROM ref.nokbdb where close_date = to_date('repDate', 'dd.MM.yyyy') and is_last = 1";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR_NOKBDB'";
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
                Nokbdb dt = new Nokbdb(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Nokbdb getById(String id){
        return (Nokbdb) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}
