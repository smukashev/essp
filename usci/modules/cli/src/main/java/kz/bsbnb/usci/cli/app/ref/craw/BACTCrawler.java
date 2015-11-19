package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.BACT;
import kz.bsbnb.usci.cli.app.ref.reps.BACTRepository;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 12/06/2015.
 */
public class BACTCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_ba_ct";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return BACT.class;
    }

    public BACTCrawler() {
       repositoryInstance = new BACTRepository();
    }
}
