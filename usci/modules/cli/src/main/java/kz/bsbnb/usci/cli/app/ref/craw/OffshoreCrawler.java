package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Offshore;
import kz.bsbnb.usci.cli.app.ref.reps.OffshoreRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 18:09
 * To change this template use File | Settings | File Templates.
 */
public class OffshoreCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_offshore";
    }

    @Override
    public HashMap getRepository() {
        return OffshoreRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Offshore.class;
    }
}
