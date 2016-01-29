package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.ExclDoc;
import kz.bsbnb.usci.cli.app.ref.reps.ExclDocRepository;


/**
 * Created by Bauyrzhan.Ibraimov on 25.01.2016.
 */
public class ExclDocCrawler extends BaseCrawler {

    @Override

    public Class getRef() {
        return ExclDoc.class;
    }


    @Override
    public String getClassName() {
        return "ref_exclusive_doc";
    }


    public ExclDocCrawler() {
        repositoryInstance = new ExclDocRepository();
    }
}
