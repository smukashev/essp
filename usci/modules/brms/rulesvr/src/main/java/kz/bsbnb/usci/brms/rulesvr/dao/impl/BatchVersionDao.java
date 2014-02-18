package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import kz.bsbnb.usci.brms.rulesvr.dao.IBatchVersionDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public class BatchVersionDao implements IBatchVersionDao {
    private JdbcTemplate jdbcTemplate;


    public boolean testConnection()
    {
        try
        {
            return !jdbcTemplate.getDataSource().getConnection().isClosed();
        }
        catch (SQLException e)
        {
            return false;
        }
    }


    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public long saveBatchVersion(Batch batch){
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

        String SQL = "INSERT INTO package_versions(package_id,repdate) VALUES(?,?)";
        jdbcTemplate.update(SQL,batch.getId(),batch.getRepoDate());

        SQL = "SELECT id FROM package_versions WHERE repdate = ? AND package_id = ?";
        long id = jdbcTemplate.queryForLong(SQL,batch.getRepoDate(),batch.getId());

        return id;
    }

    @Override
    public long saveBatchVersion(Batch batch, Date date) {
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

        String SQL = "INSERT INTO package_versions(package_id,repdate) VALUES(?,?)";
        jdbcTemplate.update(SQL,batch.getId(),date);

        SQL = "SELECT id FROM package_versions WHERE repdate = ? AND package_id = ?";
        long id = jdbcTemplate.queryForLong(SQL,date,batch.getId());

        return id;
    }

    public BatchVersion getBatchVersion(Batch batch){
       List<BatchVersion> batchVersionList = getBatchVersions(batch);
       BatchVersion batchVersion = batchVersionList.get(0);

        for (BatchVersion b: batchVersionList){
            if (b.getRepDate().getTime()>batchVersion.getRepDate().getTime()){
                batchVersion = b;
            }
        }
       return batchVersion;
    }

    @Override
    public BatchVersion getBatchVersion(Batch batch, Date date) {
        List<BatchVersion> batchVersionList = getBatchVersions(batch);
        if (batchVersionList.size()==0)return null;
        BatchVersion batchVersion = batchVersionList.get(0);

        boolean nn=false;
        for (BatchVersion b: batchVersionList){
            if (b.getRepDate().before(date) || DateUtils.isSameDay(b.getRepDate(),date)){
                batchVersion = b;
                nn=true;
                break;
            }
        }

        for (BatchVersion b: batchVersionList){
            if (b.getRepDate().before(date) && b.getRepDate().after(batchVersion.getRepDate())){
                batchVersion = b;
                nn=true;
            }
        }
        if (!nn) return null;
        return batchVersion;
    }

    public List<BatchVersion> getBatchVersions(Batch batch){

        if (batch.getId() < 1)
        {
            throw new IllegalArgumentException("Batch id can not be null");
        }

        String SQL = "SELECT * FROM package_versions WHERE package_id  = ?";

        List<BatchVersion> batchVersionList = jdbcTemplate.query(SQL, new Object[]{batch.getId()}, new BeanPropertyRowMapper(BatchVersion.class));
        return batchVersionList;
    }

    @Override
    public void copyRule(Long ruleId, Batch batch, Date versionDate) {
        List<BatchVersion> batchVersionList = getBatchVersions(batch);
        boolean nn=false;
        Long batchVersionId=0L;
        for (BatchVersion b: batchVersionList){
            if (DateUtils.isSameDay(b.getRepDate(),versionDate)){
                batchVersionId = b.getId();
                nn=true;
                break;
            }
        }
        if (nn){
            String SQL = "INSERT INTO rule_package_versions(rule_id,package_versions_id) VALUES(?,?)";
            jdbcTemplate.update(SQL,ruleId,batchVersionId);

        } else{
            Long id = saveBatchVersion(batch,versionDate);
            String SQL = "INSERT INTO rule_package_versions(rule_id,package_versions_id) VALUES(?,?)";
            jdbcTemplate.update(SQL,ruleId,id);
        }

    }

    @Override
    public BatchVersion getBatchVersion(String name, Date repdate) {
        String Sql = "SELECT t1.name, t2.id, t2.package_id, t2.repdate" +
                " from packages t1,package_versions t2" +
                " where t1.id = t2.package_id" +
                " and   t1.name = ?" +
                " AND   t2.repdate <= ?" +
                " ORDER BY t2.repdate desc" +
                " limit 1";

        return (BatchVersion) jdbcTemplate.queryForObject(Sql, new BeanPropertyRowMapper(BatchVersion.class),
                new Object[]{name, repdate});
    }
}
