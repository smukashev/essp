package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditType;
import kz.bsbnb.usci.cli.app.ref.reps.CreditTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public class CreditTypeCrawler extends BaseCrawler{
    @Override
    public String getClassName() {
        return "ref_credit_type";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return CreditType.class;
    }

    public CreditTypeCrawler() {
       repositoryInstance = new CreditTypeRepository();
    }
}
