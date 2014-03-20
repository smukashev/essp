package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.Nokbdb;
import kz.bsbnb.usci.cli.app.ref.reps.NokbdbRepository;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 18.12.13
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class NokbdbCrawler extends BaseCrawler{

    public Class getRef(){
        return Nokbdb.class;
    }

    @Override
    public String getClassName() {
        return "ref_nokbdb";    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public HashMap getRepository() {
        return NokbdbRepository.getRepository();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
