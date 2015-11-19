package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.DocType;
import kz.bsbnb.usci.cli.app.ref.reps.DocTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:41
 * To change this template use File | Settings | File Templates.
 */
public class DocTypeCrawler extends BaseCrawler {

    @Override
    public String getClassName() {
        return "ref_doc_type";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return DocType.class;
    }

    public DocTypeCrawler() {
       repositoryInstance = new DocTypeRepository();
    }
}
