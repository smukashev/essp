package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.BankRelation;
import kz.bsbnb.usci.cli.app.ref.reps.BankRelationRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class BankRelationCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_bank_relation";
    }

    @Override
    public HashMap getRepository() {
        return BankRelationRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return BankRelation.class;
    }
}
