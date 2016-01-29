package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.DocTypeCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.ExclDoc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Bauyrzhan.Ibraimov on 25.01.2016.
 */
public class ExclDocRepository extends BaseRepository {

    DocTypeCrawler docTypeCrawler;

    public ExclDocRepository(){
        QUERY_ALL ="select * from REF.SPECIAL_DOC_NO";
        QUERY_OPEN = "select * from REF.SPECIAL_DOC_NO";
        QUERY_CLOSE = "select * from REF.SPECIAL_DOC_NO where 1=2";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='SPECIAL_DOC_NO'";

        docTypeCrawler = new DocTypeCrawler();
        docTypeCrawler.constructAll();
    }

    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            DocTypeRepository docTypeRepository = (DocTypeRepository) docTypeCrawler.getRepositoryInstance();

            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    try {
                        tmp.put((String) s, rows.getString((String) s));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                tmp.put("doc_type", docTypeRepository.getById((String)tmp.get("TYPE_ID")));
                ExclDoc dt = new ExclDoc(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ExclDoc[] getByProperty(String key,String value){
        ExclDoc [] ret = new ExclDoc[0];
        List<ExclDoc> list = new ArrayList<ExclDoc>();
        for(Object v: getRepository().values()){
            if(((ExclDoc) v).get(key) != null)
                if(((ExclDoc) v).get(key).equals(value)) list.add((ExclDoc)v);
        }
        return list.toArray(ret);
    }


    public ExclDoc getById(String id){
        return (ExclDoc) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }

}
