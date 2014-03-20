package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.BaseRef;
import kz.bsbnb.usci.cli.app.ref.refs.Creditor;
import kz.bsbnb.usci.cli.app.ref.reps.CreditorRepository;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 10.10.13
 * Time: 9:39
 * To change this template use File | Settings | File Templates.
 */
public class CreditorCrawler extends BaseCrawler{


    @Override
    public HashMap getRepository() {
        return CreditorRepository.getRepository();
    }

    @Override
    public Class getRef() {
        return Creditor.class;
    }

    @Override
    public String getClassName() {
        return "ref_creditor";
    }

}
