package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.CreditorDocCrawler;
import kz.bsbnb.usci.cli.app.ref.craw.NokbdbCrawler;
import kz.bsbnb.usci.cli.app.ref.craw.SubjectTypeCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Creditor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditorRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    //private static String QUERY = "SELECT t.* FROM ref.CREDITOR_HIS WHERE main_office_id IS NULL";
    private static String QUERY = "SELECT c.* FROM ref.v_Creditor_His t, ref.creditor c WHERE c.id = t.id and t.main_office_id IS NULL\n" +
            " and t.open_date = to_date('repDate', 'dd.MM.yyyy')\n" +
            " and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null)" +
            " and exists (select 1 from ref.subject_type st where st.id = t.subject_type_id and st.open_date <= to_date('repDate', 'dd.MM.yyyy'))";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR'";*/

    public CreditorRepository() {
        QUERY_ALL = "SELECT * FROM ref.v_creditor_his";
        QUERY_OPEN = "SELECT * FROM ref.v_creditor_his where open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (close_date > to_date('repDate','dd.MM.yyyy') or close_date is null) and main_office_id is null";
        QUERY_CLOSE = "SELECT * FROM ref.v_creditor_his where shutdown_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and main_office_id is null";
        COLUMNS_QUERY = "SELECT * from (" +
                " select 'ID' as column_name from dual union all" +
                " select 'MAIN_OFFICE_ID' as column_name from dual union all" +
                " select 'SUBJECT_TYPE_ID' as column_name from dual union all" +
                " select 'SHORT_NAME' as column_name from dual union all" +
                " select 'CODE' as column_name from dual union all" +
                " select 'NAME' as column_name from dual)";

        creditorDocCrawler = new CreditorDocCrawler();
        creditorDocCrawler.constructAll();
        subjectTypeCrawler = new SubjectTypeCrawler();
        subjectTypeCrawler.constructAll();
        nokbdbCrawler = new NokbdbCrawler();
        nokbdbCrawler.constructAll();
    }

    CreditorDocCrawler creditorDocCrawler;
    SubjectTypeCrawler subjectTypeCrawler;
    NokbdbCrawler nokbdbCrawler;


    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();
            CreditorDocRepository creditorDocRepository = (CreditorDocRepository) creditorDocCrawler.getRepositoryInstance();
            SubjectTypeRepository subjectTypeRepository = (SubjectTypeRepository) subjectTypeCrawler.getRepositoryInstance();
            NokbdbRepository nokbdbRepository = (NokbdbRepository) nokbdbCrawler.getRepositoryInstance();

            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));
            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    try {
                        tmp.put(s, rows.getString((String) s));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                tmp.put("docs",creditorDocRepository.getByProperty("CREDITOR_ID",(String)tmp.get("ID")));
                tmp.put("subject_type",subjectTypeRepository.getById((String)tmp.get("SUBJECT_TYPE_ID")));
                tmp.put("nokbdb",nokbdbRepository.getById((String)tmp.get("ID")));
                Creditor dt = new Creditor(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Creditor[] getByProperty(String key,String value){
        Creditor [] ret = new Creditor[0];
        List<Creditor> list = new ArrayList<Creditor>();
        for(Object v: getRepository().values()){
            if(((Creditor) v).get(key) != null)
                if(((Creditor) v).get(key).equals(value)) list.add((Creditor)v);
        }
        return list.toArray(ret);
    }

    public Creditor getById(String id){
        return (Creditor) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

