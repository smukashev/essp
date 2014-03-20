package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.LegalForm;
import kz.bsbnb.usci.cli.app.ref.reps.LegalFormRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 18:08
 * To change this template use File | Settings | File Templates.
 */
public class LegalFormCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_legal_form";
    }

    @Override
    public HashMap getRepository() {
        return LegalFormRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return LegalForm.class;
    }
}
