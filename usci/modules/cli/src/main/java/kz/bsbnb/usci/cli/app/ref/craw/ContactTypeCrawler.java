package kz.bsbnb.usci.cli.app.ref.craw;

import kz.bsbnb.usci.cli.app.ref.BaseCrawler;
import kz.bsbnb.usci.cli.app.ref.refs.ContactType;
import kz.bsbnb.usci.cli.app.ref.reps.ContactTypeRepository;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 11.10.13
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public class ContactTypeCrawler extends BaseCrawler{

    @Override
    public String getClassName() {
        return "ref_contact_type";
    }

    @Override
    public HashMap getRepository() {
        return getRepositoryInstance().getRepository();
    }

    @Override
    public Class getRef() {
        return ContactType.class;
    }

    public ContactTypeCrawler() {
       repositoryInstance = new ContactTypeRepository();
    }
}
