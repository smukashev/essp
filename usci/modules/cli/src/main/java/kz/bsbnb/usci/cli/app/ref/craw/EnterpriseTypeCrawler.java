package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.EnterpriseType;
import kz.bsbnb.usci.cli.app.ref.reps.EnterpriseTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */
public class EnterpriseTypeCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_enterprise_type";
    }

    @Override
    public HashMap getRepository() {
        return EnterpriseTypeRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return EnterpriseType.class;
    }
}
