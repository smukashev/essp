package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.dao.mapper.BatchMapper;
import kz.bsbnb.usci.brms.rulesvr.model.IBatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.usci.brms.rulesvr.generated.Tables.*;

/**
 * @author abukabayev
 * @modified k.tulbassiyev
 */


public class BatchDao implements IBatchDao
{
    private JdbcTemplate jdbcTemplate;

    private final String PREFIX_ = "LOGIC_";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    public BatchDao() {
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
            throw new IllegalArgumentException("Does not have id. Can't load.");

        String SQL = "SELECT * FROM " + PREFIX_ + "packages WHERE id  = ?";
        Batch batch = jdbcTemplate.queryForObject(SQL,new Object[]{id},new BatchMapper());
        return batch;
    }


    private long saveBatch(Batch batch){

        if (batch.getRepDate() == null)
        {
            throw new IllegalArgumentException("Report date must be set before instance " +
                    "of Batch saving to the DB.");
        }

        String SQL = "INSERT INTO " + PREFIX_ + "packages(NAME, REPORT_DATE) VALUES (?, ?)";
        jdbcTemplate.update(SQL,batch.getName(),batch.getRepDate());
        System.out.println("Created batch with repodate"+batch.getRepDate()+" called "+batch.getName());

        SQL = "SELECT id FROM " + PREFIX_ + "packages WHERE NAME = ?";
        long id = jdbcTemplate.queryForLong(SQL,batch.getName());
        return id;
    }

    private void saveBatchVersion(Batch batch,long batchId){

        if(batchId < 1)
        {
            throw new IllegalArgumentException("Batch does not have id. Can't create batch version.");
        }

       String SQL = "INSERT INTO " + PREFIX_ + "package_versions(package_id, REPORT_DATE) VALUES(?, ?)";
        jdbcTemplate.update(SQL,batchId,batch.getRepDate());
    }


    @Override
    public long save(Batch batch) {
        long batchId = saveBatch(batch);
//        saveBatchVersion(batch,batchId);
        return batchId;
    }

    @Override
    public List<Batch> getAllBatches() {
        String SQL = "SELECT * FROM " + PREFIX_ + "packages";
        List<Batch> batchList = jdbcTemplate.query(SQL, new BeanPropertyRowMapper(Batch.class));
        return batchList;
    }

    @Override
    public long getBatchVersionId(long batchId, Date repDate) {
        String SQL = "SELECT id FROM " + PREFIX_ + "package_versions WHERE OPEN_DATE = \n" +
                "     (SELECT MAX(OPEN_DATE) FROM " + PREFIX_ + "package_versions WHERE package_id = ? AND OPEN_DATE <= ? ) \n" +
                "AND package_id = ? AND rownum = 1";

        return jdbcTemplate.queryForLong(SQL, batchId, repDate, batchId);
    }

    @Override
    public List<IBatchVersion> getBatchVersions(long batchId) {
        Select select = context.select(LOGIC_PACKAGE_VERSIONS.ID, LOGIC_PACKAGE_VERSIONS.OPEN_DATE)
                .from(LOGIC_PACKAGE_VERSIONS)
                .where(LOGIC_PACKAGE_VERSIONS.PACKAGE_ID.eq(batchId));

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<IBatchVersion> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            IBatchVersion batchVersion = new BatchVersion();
            batchVersion.setId(((BigDecimal)row.get(LOGIC_PACKAGE_VERSIONS.ID.getName())).longValue());
            batchVersion.setPackageId(batchId);
            batchVersion.setOpenDate((Date)row.get(LOGIC_PACKAGE_VERSIONS.OPEN_DATE.getName()));
            ret.add(batchVersion);
        }

        return ret;
    }
}
