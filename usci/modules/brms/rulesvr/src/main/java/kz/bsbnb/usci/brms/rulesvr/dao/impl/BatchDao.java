package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.dao.mapper.BatchMapper;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */


public class BatchDao implements IBatchDao
{
    private JdbcTemplate jdbcTemplate;

    public BatchDao(){

    }


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

    @Override
    public Batch loadBatch(long id) {

        if(id < 1)
        {
            throw new IllegalArgumentException("Does not have id. Can't load.");
        }

        String SQL = "SELECT * FROM packages WHERE id  = ?";
        Batch batch = jdbcTemplate.queryForObject(SQL,new Object[]{id},new BatchMapper());
        return batch;
    }


    private long saveBatch(Batch batch){

        if (batch.getRepoDate() == null)
        {
            throw new IllegalArgumentException("Report date must be set before instance " +
                    "of Batch saving to the DB.");
        }

        String SQL = "INSERT INTO packages(NAME,repdate) VALUES (?,?)";
        jdbcTemplate.update(SQL,batch.getName(),batch.getRepoDate());
        System.out.println("Created batch with repodate"+batch.getRepoDate()+" called "+batch.getName());

        SQL = "SELECT id FROM packages WHERE NAME = ?";
        long id = jdbcTemplate.queryForLong(SQL,batch.getName());
        return id;
    }

    private void saveBatchVersion(Batch batch,long batchId){

        if(batchId < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

       String SQL = "INSERT INTO package_versions(package_id,repdate) VALUES(?,?)";
        jdbcTemplate.update(SQL,batchId,batch.getRepoDate());
    }


    @Override
    public long save(Batch batch) {
        long batchId = saveBatch(batch);
//        saveBatchVersion(batch,batchId);
        return batchId;
    }

    @Override
    public List<Batch> getAllBatches() {
        String SQL = "SELECT * FROM packages";
        List<Batch> batchList = jdbcTemplate.query(SQL,new BeanPropertyRowMapper(Batch.class));
        return batchList;
    }

    @Override
    public long getBatchVersionId(long batchId, Date repDate) {
        String SQL = "SELECT id FROM package_versions WHERE repdate = \n" +
                "  (SELECT MAX(repdate)\n" +
                "     FROM\n" +
                "     (SELECT * FROM package_versions WHERE package_id = ? AND repdate <= ? ) tm\n" +
                "        )\n" +
                "AND package_id = ? LIMIT 1";

        return jdbcTemplate.queryForLong(SQL, batchId, repDate, batchId);
    }
}
