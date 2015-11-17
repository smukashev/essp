package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.DocTypeCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorDoc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditorDocRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT * FROM ref.CREDITOR_DOC";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR_DOC'";*/

    public CreditorDocRepository() {
        QUERY_ALL = "SELECT * FROM ref.creditor_doc";
        QUERY_OPEN = "SELECT * FROM ref.creditor_doc";
        QUERY_CLOSE = "SELECT * FROM ref.creditor_doc_his where close_date = to_date('repDate', 'dd.MM.yyyy')";
        COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR_DOC'";
    }

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();

            DocTypeCrawler docTypeCrawler = new DocTypeCrawler();
            docTypeCrawler.constructAll();
            DocTypeRepository docTypeRepository = (DocTypeRepository) docTypeCrawler.getRepositoryInstance();

            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));

            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                tmp.put("doc_type",docTypeRepository.getById((String)tmp.get("TYPE_ID")));
                CreditorDoc dt = new CreditorDoc(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CreditorDoc[] getByProperty(String key,String value){
        CreditorDoc [] ret = new CreditorDoc[0];
        List<CreditorDoc> list = new ArrayList<CreditorDoc>();
        for(Object v: getRepository().values()){
            if(((CreditorDoc) v).get(key) != null)
                if(((CreditorDoc) v).get(key).equals(value)) list.add((CreditorDoc)v);
        }
        return list.toArray(ret);
    }

    public CreditorDoc getById(String id){
        return (CreditorDoc) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

