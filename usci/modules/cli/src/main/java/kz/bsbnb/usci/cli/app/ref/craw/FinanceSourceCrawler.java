package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.FinanceSource;
import kz.bsbnb.usci.cli.app.ref.reps.FinanceSourceRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public class FinanceSourceCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_finance_source";
    }

    @Override
    public HashMap getRepository() {
        return FinanceSourceRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return FinanceSource.class;
    }
}
