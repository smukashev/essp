package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Meta;
import kz.bsbnb.usci.cli.app.ref.reps.MetaRepository;

import java.util.HashMap;

public class MetaCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_meta";
    }

    @Override
    public HashMap getRepository() {
        return MetaRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Meta.class;
    }
}
