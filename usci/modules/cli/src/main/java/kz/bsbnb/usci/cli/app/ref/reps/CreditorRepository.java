package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.Creditor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditorRepository extends BaseRepository {
    private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT t.* FROM ref.CREDITOR_HIS WHERE main_office_id IS NULL";
    private static String QUERY = "SELECT c.* FROM ref.v_Creditor_His t, ref.creditor c WHERE c.id = t.id and t.main_office_id IS NULL\n" +
            " and t.open_date <= to_date('" + repDate + "', 'dd.MM.yyyy')\n" +
            " and (t.close_date > to_date('" + repDate + "', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR'";

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
                tmp.put("docs",CreditorDocRepository.getByProperty("CREDITOR_ID",(String)tmp.get("ID")));
                tmp.put("subject_type",SubjectTypeRepository.getById((String)tmp.get("SUBJECT_TYPE")));
                tmp.put("nokbdb",NokbdbRepository.getById((String)tmp.get("ID")));
                Creditor dt = new Creditor(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Creditor[] getByProperty(String key,String value){
        Creditor [] ret = new Creditor[0];
        List<Creditor> list = new ArrayList<Creditor>();
        for(Object v: getRepository().values()){
            if(((Creditor) v).get(key) != null)
                if(((Creditor) v).get(key).equals(value)) list.add((Creditor)v);
        }
        return list.toArray(ret);
    }

    public static Creditor getById(String id){
        return (Creditor) getRepository().get(id);
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

