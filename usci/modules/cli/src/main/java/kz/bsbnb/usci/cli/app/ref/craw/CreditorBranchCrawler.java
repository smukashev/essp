package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorBranch;
import kz.bsbnb.usci.cli.app.ref.reps.CreditorBranchRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
public class CreditorBranchCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_creditor_branch";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return CreditorBranch.class;
    }

    public CreditorBranchCrawler() {
       repositoryInstance = new CreditorBranchRepository();
    }
}
