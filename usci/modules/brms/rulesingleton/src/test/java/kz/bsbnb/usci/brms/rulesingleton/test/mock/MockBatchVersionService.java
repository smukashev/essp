package kz.bsbnb.usci.brms.rulesingleton.test.mock;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchVersionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MockBatchVersionService implements IBatchVersionService
{
    @Override
    public BatchVersion load(Batch batch, Date date)
    {
        //not used in tests
        return null;
    }

    @Override
    public long save(Batch batch)
    {
        //not used in tests
        return 0;
    }

    @Override
    public long save(Batch batch, Date date)
    {
        //not used in tests
        return 0;
    }

    @Override
    public List<BatchVersion> getBatchVersions(Batch batch)
    {
        List<BatchVersion> versions = new ArrayList<BatchVersion>();

        BatchVersion version = new BatchVersion(new Date(2001, 1, 1), batch.getId());
        version.setId(1);

        versions.add(version);

        return versions;
    }

    @Override
    public void copyRule(Long ruleId, Batch batch, Date versionDate)
    {
        //not used in tests
    }

    @Override
    public BatchVersion getBatchVersion(String batchName, Date date) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
