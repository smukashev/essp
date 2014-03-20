package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditPurpose;
import kz.bsbnb.usci.cli.app.ref.reps.CreditPurposeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class CreditPurposeCrawler extends BaseCrawler {

    @Override
    public String getClassName() {
        return "ref_credit_purpose";
    }

    @Override
    public HashMap getRepository() {
        return CreditPurposeRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return CreditPurpose.class;
    }
}
