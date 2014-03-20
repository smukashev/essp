package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Currency;
import kz.bsbnb.usci.cli.app.ref.reps.CurrencyRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:40
 * To change this template use File | Settings | File Templates.
 */
public class CurrencyCrawler extends BaseCrawler {

    @Override
    public String getClassName() {
        return "ref_currency";
    }

    @Override
    public HashMap getRepository() {
        return CurrencyRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Currency.class;
    }
}
