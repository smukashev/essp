package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.BalanceAccount;
import kz.bsbnb.usci.cli.app.ref.reps.BalanceAccountRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class BalanceAccountCrawler extends BaseCrawler {

    @Override
    public String getClassName() {
        return "ref_balance_account";
    }

    @Override
    public Class getRef() {
        return BalanceAccount.class;
    }

    public BalanceAccountCrawler() {
       repositoryInstance = new BalanceAccountRepository();
    }
}
