package kz.bsbnb.usci.brms.rulesingleton.test.mock;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MockBatchService implements IBatchService
{
    @Override
    public long save(Batch batch)
    {
        //not used in tests
        return 0;
    }

    @Override
    public Batch load(long id)
    {
        //not used in tests
        return null;
    }

    @Override
    public List<Batch> getAllBatches()
    {
        List<Batch> allBatches = new ArrayList<Batch>();
        Batch batch = new Batch("drl", new Date());
        batch.setId(1);

        allBatches.add(batch);

        return allBatches;
    }
}
