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
    public Class getRef() {
        return Creditor.class;
    }

    @Override
    public String getClassName() {
        return "ref_creditor";
    }

    //    @Override
//    public void buildElement(Element root) {
//
//          int i = 0;
//         for( Object o: CreditorRepository.getRepository().values()){
//
//             if(i>5) break;
//             Creditor creditor = (Creditor) o;
//
//             Element entity = getDocument().createElement("entity");
//             root.appendChild(entity);
//
//             Attr attr = getDocument().createAttribute("class");
//             attr.setValue("ref_creditor");
//             entity.setAttributeNode(attr);
//
//             creditor.buildElement(entity);
//
//             /*appendToElement(entity,"name",creditor.get("NAME"));
//             appendToElement(entity,"short_name",creditor.get("SHORT_NAME"));*/
//
//             i++;
//          }
//
////         Element firstname = getDocument().createElement("firstname");
////         firstname.appendChild(getDocument().createTextNode("Bauyrzan"));
////         root.appendChild(firstname);
////
////         Element lastname = getDocument().createElement("lastname");
////         lastname.appendChild(getDocument().createTextNode("Makhambetov"));
////         root.appendChild(lastname);
//
//
//
//    }

    public CreditorCrawler() {
       repositoryInstance = new CreditorRepository();
    }
}
