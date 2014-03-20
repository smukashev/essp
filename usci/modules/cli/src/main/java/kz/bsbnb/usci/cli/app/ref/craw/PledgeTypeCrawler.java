package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.PledgeType;
import kz.bsbnb.usci.cli.app.ref.reps.PledgeTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */
public class PledgeTypeCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_pledge_type";
    }

    @Override
    public HashMap getRepository() {
        return PledgeTypeRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return PledgeType.class;
    }
}
