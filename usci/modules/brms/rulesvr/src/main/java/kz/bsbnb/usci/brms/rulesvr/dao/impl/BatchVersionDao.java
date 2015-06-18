package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.persistable.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import kz.bsbnb.usci.brms.rulesvr.dao.IBatchVersionDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;

import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public class BatchVersionDao extends JDBCSupport implements IBatchVersionDao  {
    private final String PREFIX_ = "LOGIC_";

    public long saveBatchVersion(final Batch batch){
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

        String SQL = "INSERT INTO " + PREFIX_ + "package_versions(package_id, REPORT_DATE) VALUES(?, ?)";
        return insertWithId(SQL, new Object[]{batch.getId(), DataUtils.convert(batch.getRepDate())});
    }

    @Override
    public long saveBatchVersion(Batch batch, Date date) {
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

        String SQL = "INSERT INTO " + PREFIX_ + "package_versions(package_id, REPORT_DATE) VALUES(?, ?)";
        return insertWithId(SQL,new Object[]{batch.getId(), DataUtils.convert(date)});
    }

    public BatchVersion getBatchVersion(Batch batch){
       List<BatchVersion> batchVersionList = getBatchVersions(batch);
       BatchVersion batchVersion = batchVersionList.get(0);

        for (BatchVersion b: batchVersionList){
            if (b.getOpenDate().getTime()>batchVersion.getOpenDate().getTime()){
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
            if (b.getOpenDate().before(date) || DateUtils.isSameDay(b.getOpenDate(),date)){
                batchVersion = b;
                nn=true;
                break;
            }
        }

        for (BatchVersion b: batchVersionList){
            if (b.getOpenDate().before(date) && b.getOpenDate().after(batchVersion.getOpenDate())){
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

        String SQL = "SELECT * FROM " + PREFIX_ + "package_versions WHERE package_id  = ?";

        List<BatchVersion> batchVersionList = jdbcTemplate.query(SQL, new Object[]{batch.getId()},
                new BeanPropertyRowMapper(BatchVersion.class));
        return batchVersionList;
    }

    @Override
    public void copyRule(Long ruleId, Batch batch, Date versionDate) {
        List<BatchVersion> batchVersionList = getBatchVersions(batch);
        boolean nn=false;
        Long batchVersionId=0L;
        for (BatchVersion b: batchVersionList){
            if (DateUtils.isSameDay(b.getOpenDate(),versionDate)){
                batchVersionId = b.getId();
                nn=true;
                break;
            }
        }
        if (nn){
            String SQL = "INSERT INTO " + PREFIX_ + "rule_package_versions(rule_id, package_versions_id) VALUES(?, ?)";
            jdbcTemplate.update(SQL,ruleId,batchVersionId);

        } else{
            Long id = saveBatchVersion(batch,versionDate);
            String SQL = "INSERT INTO " + PREFIX_ + "rule_package_versions(rule_id,package_versions_id) VALUES(?,?)";
            jdbcTemplate.update(SQL,ruleId,id);
        }

    }

    @Override
    public BatchVersion getBatchVersion(String name, Date repdate) {
        String Sql = "SELECT t1.name, t2.id, t2.package_id, t2.REPORT_DATE" +
                " from " + PREFIX_ + "packages t1," + PREFIX_ + "package_versions t2" +
                " where t1.id = t2.package_id" +
                " and   t1.name = ?" +
                " AND   t2.REPORT_DATE <= ? AND ROWNUM = 1" +
                " ORDER BY t2.REPORT_DATE desc";

        return (BatchVersion) jdbcTemplate.queryForObject(Sql, new BeanPropertyRowMapper(BatchVersion.class),
                new Object[]{name, repdate});
    }
}
