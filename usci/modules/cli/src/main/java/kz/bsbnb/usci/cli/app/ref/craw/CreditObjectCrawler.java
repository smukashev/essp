package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditObject;
import kz.bsbnb.usci.cli.app.ref.reps.CreditObjectRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class CreditObjectCrawler extends BaseCrawler{
    @Override
    public String getClassName() {
        return "ref_credit_object";
    }

    @Override
    public HashMap getRepository() {
        return CreditObjectRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return CreditObject.class;
    }
}
