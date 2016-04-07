package kz.bsbnb.usci.brms.rulesvr.dao.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IPackageVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulesvr.dao.IPackageDao;
import kz.bsbnb.usci.brms.rulesvr.dao.mapper.BatchMapper;
import kz.bsbnb.usci.brms.rulesvr.persistable.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.usci.brms.rulesvr.generated.Tables.*;

/**
 * @author abukabayev
 * @modified k.tulbassiyev
 */


public class PackageDao extends JDBCSupport implements IPackageDao
{
    private final String PREFIX_ = "LOGIC_";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    public PackageDao() {
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

    @Override
    public RulePackage loadBatch(long id) {

        if(id < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E259));

        String SQL = "SELECT * FROM " + PREFIX_ + "packages WHERE id  = ?";
        RulePackage batch = jdbcTemplate.queryForObject(SQL,new Object[]{id},new BatchMapper());
        return batch;
    }


    private long savePackage(RulePackage rulePackage){
        /*
        if (rulePackage.getReportDate() == null)
        {
            throw new IllegalArgumentException("Report date must be set before instance " +
                    "of Batch saving to the DB.");
        }*/

        Insert insert = context.insertInto(LOGIC_PACKAGES)
                .set(LOGIC_PACKAGES.NAME, rulePackage.getName());


        //jdbcTemplate.update(insert.getSQL(),insert.getBindValues().toArray());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        /*DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println("Created batch with repdate "+dateFormat.format(rulePackage.getReportDate())+" called "+rulePackage.getName());

        SQL = "SELECT id FROM " + PREFIX_ + "packages WHERE NAME = ?";
        long id = jdbcTemplate.queryForLong(SQL,rulePackage.getName());
        return id;*/
    }

    private void saveBatchVersion(RulePackage batch, long batchId){

        /*
        if(batchId < 1)
        {
            throw new IllegalArgumentException(Errors.compose(Errors.E261));
        }

       String SQL = "INSERT INTO " + PREFIX_ + "package_versions(package_id, REPORT_DATE) VALUES(?, ?)";
        jdbcTemplate.update(SQL,batchId,batch.getReportDate());*/
    }


    @Override
    public long save(RulePackage rulePackage) {
        long batchId = savePackage(rulePackage);
//        saveBatchVersion(batch,batchId);
        return batchId;
    }

    @Override
    public List<RulePackage> getAllPackages() {
        Select select = context.selectFrom(LOGIC_PACKAGES);

        List<Map<String,Object> > rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<RulePackage> batchList = new ArrayList<>();

        for(Map<String,Object> row: rows) {
            RulePackage b = new RulePackage();
            b.setId(((BigDecimal) row.get(LOGIC_PACKAGES.ID.getName())).longValue());
            b.setName(((String) row.get(LOGIC_PACKAGES.NAME.getName())));
            batchList.add(b);
        }

        return batchList;
    }

    @Override
    public long getBatchVersionId(long batchId, Date repDate) {
        String SQL = "SELECT id FROM " + PREFIX_ + "package_versions WHERE OPEN_DATE = \n" +
                "     (SELECT MAX(OPEN_DATE) FROM " + PREFIX_ + "package_versions WHERE package_id = ? AND OPEN_DATE <= ? ) \n" +
                "AND package_id = ? AND rownum = 1";

        return jdbcTemplate.queryForLong(SQL, batchId, repDate, batchId);
    }

    /*@Override
    public List<IPackageVersion> getBatchVersions(long batchId) {
        Select select = context.select(LOGIC_PACKAGE_VERSIONS.ID, LOGIC_PACKAGE_VERSIONS.OPEN_DATE)
                .from(LOGIC_PACKAGE_VERSIONS)
                .where(LOGIC_PACKAGE_VERSIONS.PACKAGE_ID.eq(batchId))
                .orderBy(LOGIC_PACKAGE_VERSIONS.OPEN_DATE.desc());

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        List<IBatchVersion> ret = new LinkedList<>();

        for(Map<String,Object> row: rows) {
            IBatchVersion batchVersion = new BatchVersion();
            batchVersion.setId(((BigDecimal)row.get(LOGIC_PACKAGE_VERSIONS.ID.getName())).longValue());
            batchVersion.setPackageId(batchId);
            batchVersion.setReportDate((Date)row.get(LOGIC_PACKAGE_VERSIONS.OPEN_DATE.getName()));
            ret.add(batchVersion);
        }

        return ret;
    }*/
}
