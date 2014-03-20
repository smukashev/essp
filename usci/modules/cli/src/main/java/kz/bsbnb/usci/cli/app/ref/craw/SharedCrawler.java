package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Shared;
import kz.bsbnb.usci.cli.app.ref.reps.SharedRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */
public class SharedCrawler extends BaseCrawler{
    @Override
    public String getClassName() {
        return "ref_shared";
    }

    @Override
    public HashMap getRepository() {
        return SharedRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Shared.class;
    }
}
