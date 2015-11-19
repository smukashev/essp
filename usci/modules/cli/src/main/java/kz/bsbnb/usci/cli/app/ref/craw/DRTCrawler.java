package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.EconSector;
import kz.bsbnb.usci.cli.app.ref.reps.DRTRepository;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 13/06/2015.
 */
public class DRTCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_debt_remains_type";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return EconSector.class;
    }

    public DRTCrawler() {
       repositoryInstance = new DRTRepository();
    }
}
