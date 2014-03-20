package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.SubjectType;
import kz.bsbnb.usci.cli.app.ref.reps.SubjectTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 04.03.14
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public class SubjectTypeCrawler extends BaseCrawler {
    @Override
    public HashMap getRepository() {
        return SubjectTypeRepository.getRepository();
    }

    @Override
    public String getClassName() {
        return "ref_subject_type";
    }

    @Override
    public Class getRef() {
        return SubjectType.class;
    }
}
