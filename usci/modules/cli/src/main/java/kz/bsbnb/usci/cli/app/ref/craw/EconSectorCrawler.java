package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.reps.EconSectorRepository;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 13/04/2015.
 */
public class EconSectorCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_econ_sector";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return super.getRef();
    }

    public EconSectorCrawler() {
       repositoryInstance = new EconSectorRepository();
    }
}
