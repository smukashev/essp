package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import org.w3c.dom.Element;

import java.util.HashMap;

/**
 * Created by Bauyrzhan.Ibraimov on 25.01.2016.
 */
public class ExclDoc extends BaseRef {


    public ExclDoc(HashMap hm) {
        super(hm);
    }

    @Override
    public void buildElement(Element root) {

        appendToElement(root, "code", hm.get("NO"));
        Element doc_type = getDocument().createElement("doc_type");
        root.appendChild(doc_type);

        DocType dt = (DocType)hm.get("doc_type");
        dt.buildElement(doc_type);

    }
}
