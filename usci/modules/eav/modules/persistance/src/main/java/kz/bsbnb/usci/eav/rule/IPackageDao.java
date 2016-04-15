package kz.bsbnb.usci.eav.rule;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IPackageDao {
    RulePackage loadBatch(long id);

    long save(RulePackage batch);

    List<RulePackage> getAllPackages();

    long getBatchVersionId(long batchId, Date repDate);

    void setDataSource(DataSource dataSource);
}
