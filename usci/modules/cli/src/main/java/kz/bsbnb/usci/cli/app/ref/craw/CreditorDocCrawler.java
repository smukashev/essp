package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.CreditorDoc;
import kz.bsbnb.usci.cli.app.ref.reps.CreditorDocRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 11:56
 * To change this template use File | Settings | File Templates.
 */
public class CreditorDocCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "doc1";
    }

    @Override
    public Class getRef() {
        return CreditorDoc.class;
    }

    @Override
    public HashMap getRepository() {
        return CreditorDocRepository.getRepository();
    }
}
