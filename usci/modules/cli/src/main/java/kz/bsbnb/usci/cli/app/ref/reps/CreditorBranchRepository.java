
package kz.bsbnb.usci.cli.app.ref.reps;

import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import kz.bsbnb.usci.cli.app.ref.craw.CreditorCrawler;
import kz.bsbnb.usci.cli.app.ref.craw.CreditorDocCrawler;
import kz.bsbnb.usci.cli.app.ref.craw.RegionCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorBranch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CreditorBranchRepository extends BaseRepository {
    /*private static HashMap repository;
    private static HashSet columns;
    private static String QUERY = "SELECT c.* FROM ref.v_CREDITOR_his t, ref.creditor c WHERE c.id = t.id"  + " and t.open_date = to_date('repDate', 'dd.MM.yyyy')\n"+
            "   and (t.close_date > to_date('repDate', 'dd.MM.yyyy') or t.close_date is null) and c.main_office_id is not null";
    private static String COLUMNS_QUERY = "SELECT * FROM all_tab_cols WHERE owner = 'REF' AND TABLE_NAME='CREDITOR'";*/

    public CreditorBranchRepository() {
        QUERY_ALL = "SELECT * FROM ref.creditor_branch";
        QUERY_OPEN = "SELECT t1.*, t2.region_id FROM ref.v_creditor_his t1, ref.creditor t2 where t1.id = t2.id and t1.open_date = to_date('repDate', 'dd.MM.yyyy') " +
                " and (t1.close_date > to_date('repDate','dd.MM.yyyy') or t1.close_date is null) and t1.main_office_id is not null";
        QUERY_CLOSE = "SELECT t1.*, t2.region_id FROM ref.v_creditor_his t1, ref.creditor t2 " +
                "where t1.id = t2.id and t1.close_date = to_date('repDate', 'dd.MM.yyyy') and t1.main_office_id is not null";
        COLUMNS_QUERY = "SELECT * from (" +
                " select 'ID' as column_name from dual union all" +
                " select 'MAIN_OFFICE_ID' as column_name from dual union all" +
                " select 'SUBJECT_TYPE_ID' as column_name from dual union all" +
                " select 'SHORT_NAME' as column_name from dual union all" +
                " select 'CODE' as column_name from dual union all" +
                " select 'NAME' as column_name from dual union all" +
                " select 'REGION_ID' as column_name from dual)";
        creditorDocCrawler = new CreditorDocCrawler();
        creditorDocCrawler.constructAll();
        creditorCrawler = new CreditorCrawler();
        creditorCrawler.constructAll();
        regionCrawler = new RegionCrawler();
        regionCrawler.constructAll();
    }

    CreditorDocCrawler creditorDocCrawler;
    CreditorCrawler creditorCrawler;
    RegionCrawler regionCrawler;

    @Override
    public HashMap construct(String query){
        try {
            HashSet hs = getColumns();

            CreditorDocRepository creditorDocRepository = (CreditorDocRepository) creditorDocCrawler.getRepositoryInstance();
            CreditorRepository creditorRepository = (CreditorRepository) creditorCrawler.getRepositoryInstance();
            RegionRepository regionRepository = (RegionRepository) regionCrawler.getRepositoryInstance();

            ResultSet rows = getStatement().executeQuery(query.replaceAll("repDate",repDate));
            HashMap hm = new HashMap();
            while(rows.next()){
                HashMap tmp = new HashMap();
                //System.out.println(rows.getString("NAME_RU"));
                for(Object s: hs){
                    //System.out.println(s);
                    tmp.put((String)s,rows.getString((String)s));
                }
                tmp.put("docs",creditorDocRepository.getByProperty("CREDITOR_ID",(String) tmp.get("ID")));
                if (tmp.get("MAIN_OFFICE_ID") == null) {
                    tmp.put("main_office",creditorRepository.getById((String) tmp.get("ID")));
                } else {
                    tmp.put("main_office",creditorRepository.getById((String) tmp.get("MAIN_OFFICE_ID")));
                }

                if(tmp.get("REGION_ID") != null)
                    tmp.put("region",regionRepository.getById((String)tmp.get("REGION_ID")));
                CreditorBranch dt = new CreditorBranch(tmp);
                hm.put(dt.get(dt.getKeyName()),dt);
            }

            return hm;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CreditorBranch[] getByProperty(String key,String value){
        CreditorBranch [] ret = new CreditorBranch[0];
        List<CreditorBranch> list = new ArrayList<CreditorBranch>();
        for(Object v: getRepository().values()){
            if(((CreditorBranch) v).get(key) != null)
                if(((CreditorBranch) v).get(key).equals(value)) list.add((CreditorBranch)v);
        }
        return list.toArray(ret);
    }

    public CreditorBranch getById(String id){
        return (CreditorBranch) getRepository().get(id);
    }

    public void rc(){
        repository = null;
    }
}

