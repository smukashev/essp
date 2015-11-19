
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.Meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MetaRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM eav_m_classes";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'CORE' AND TABLE_NAME='EAV_M_CLASSES'";

    public HashMap construct(){
        try {
            ResultSet rows = getStatement().executeQuery(QUERY.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashSet hs = getColumns();
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                Meta dt = new Meta(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Meta getById(String id){
        return (Meta) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

