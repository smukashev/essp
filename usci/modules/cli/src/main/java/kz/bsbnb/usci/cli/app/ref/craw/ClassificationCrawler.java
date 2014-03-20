package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Classification;
import kz.bsbnb.usci.cli.app.ref.reps.ClassificationRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationCrawler extends BaseCrawler {
    @Override
    public String getClassName() {
        return "ref_classification";
    }

    @Override
    public HashMap getRepository() {
        return ClassificationRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Classification.class;
    }
}
