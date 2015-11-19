package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.refs.DocType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 11:14
 * To change this template use File | Settings | File Templates.
 */
public class DocTypeRepository extends BaseRepository {

    private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT * FROM ref.doc_type";
    //private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='DOC_TYPE'";

    public DocTypeRepository() {
        QUERY_CLOSE = "SELECT * FROM ref.doc_type";
        QUERY_OPEN = "SELECT * FROM ref.doc_type where open_date = to_date('repDate','dd.MM.yyyy')";
        QUERY_ALL = "SELECT * FROM ref.doc_type";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='DOC_TYPE'";
    }

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));
//            rows.next();
//            HashMap hm = new HashMap();
//            HashSet hs = getColumns();
//            for(Object s : hs){
//                hm.put(s,rows.getString(s));
//            }
//            DocType dt = new DocType(hm);
//            return hm;

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                DocType dt = new DocType(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DocType getById(String id){
         return (DocType) getRepository().get(id);
    }

    public DocType getByCode(String code) {
        for(Object d : getRepository().values()) {
            DocType dt = (DocType) d;
            if(dt.get("CODE").equals(code))
                return dt;
        }

        throw new RuntimeException("docType with code" + code + " not found");
    }

    public void rc(){
        repository = null;
    }
}
