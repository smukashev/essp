package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Region;
import kz.bsbnb.usci.cli.app.ref.reps.RegionRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class RegionCrawler extends BaseCrawler{
    @Override
    public String getClassName() {
        return "ref_region";
    }

    @Override
    public HashMap getRepository() {
        return RegionRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Region.class;
    }
}
