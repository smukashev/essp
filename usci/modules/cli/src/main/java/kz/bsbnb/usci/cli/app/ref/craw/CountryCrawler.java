package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Country;
import kz.bsbnb.usci.cli.app.ref.reps.CountryRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:02
 * To change this template use File | Settings | File Templates.
 */
public class CountryCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_country";
    }

    @Override
    public HashMap getRepository() {
        return CountryRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Country.class;
    }
}
