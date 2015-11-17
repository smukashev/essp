package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Portfolio;
import kz.bsbnb.usci.cli.app.ref.reps.PortfolioRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public class PortfolioCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_portfolio";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return Portfolio.class;
    }

    public PortfolioCrawler() {
       repositoryInstance = new PortfolioRepository();
    }
}
