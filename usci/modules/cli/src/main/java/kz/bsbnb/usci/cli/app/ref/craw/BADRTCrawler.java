package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.BADRT;
import kz.bsbnb.usci.cli.app.ref.reps.BADRTRepository;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Makhambeto on 13/06/2015.
 */
public class BADRTCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_ba_drt";
    }

    @Override
    public HashMap getRepository() {
        return BADRTRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return BADRT.class;
    }
}
