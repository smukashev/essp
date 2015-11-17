package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.EconTrade;
import kz.bsbnb.usci.cli.app.ref.reps.EconTradeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:50
 * To change this template use File | Settings | File Templates.
 */
public class EconTradeCrawler extends BaseCrawler{
    @Override
    public String getClassName() {
        return "ref_econ_trade";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return EconTrade.class;
    }

    public EconTradeCrawler() {
       repositoryInstance = new EconTradeRepository();
    }
}
