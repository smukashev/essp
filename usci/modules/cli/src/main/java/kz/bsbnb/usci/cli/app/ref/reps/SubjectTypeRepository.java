package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.SubjectType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class SubjectTypeRepository extends BaseRepository{
    private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.subject_type t" + " where t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='SUBJECT_TYPE'";

    public static HashMap getRepository() {
        if(BaseRepository.closeMode) QUERY = BaseRepository.QUERY;if(repository==null)
            repository = construct();
        return repository;
    }

    public static HashMap construct(){
        try {
            HashSet hs = getColumns();
            String Q_old;
            if(!"ref_subject_type".equals(BaseRepository.targetClass))
                QUERY = QUERY.replaceFirst("=","<=");
            ResultSet rows = getStatement().executeQuery(QUERY.replaceAll("repDate",repDate));

            if(!"ref_subject_type".equals(BaseRepository.targetClass))
                QUERY = QUERY.replaceFirst("<=","=");



            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                SubjectType dt = new SubjectType(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SubjectType[] getByProperty(String key,String value){
        SubjectType [] ret = new SubjectType[0];
        List<SubjectType> list = new ArrayList<SubjectType>();
        for(Object v: repository.values()){
            if(((SubjectType) v).get(key).equals(value)) list.add((SubjectType)v);
        }
        return list.toArray(ret);
    }

    public static SubjectType getById(String id){
        return (SubjectType) getRepository().get(id);
    }

    public static HashSet getColumns() {
        try {
            if(columns ==null){
                HashSet hs = new HashSet();
                ResultSet rows = getStatement().executeQuery(COLUMNS_QUERY);
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

    public static void rc(){
        repository = null;
    }
}
