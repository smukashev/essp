package kz.bsbnb.usci.cli.app.ref.refs;

import kz.bsbnb.usci.cli.app.ref.BaseRef;
import kz.bsbnb.usci.cli.app.ref.BaseRepository;
import org.w3c.dom.Element;
import org.w3c.dom.stylesheets.DocumentStyle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Bauyrzhan.Makhambeto
 * Date: 09.10.13
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public class Creditor extends BaseRef {
    public Creditor(HashMap hm){
        super(hm);
    }

    public String get(String s){
        return (String) hm.get(s);
    }

    public String getKeyName(){
        return "ID";
    }

    @Override
    public void buildElement(Element root) {
        appendToElement(root,"name",hm.get("NAME"));
        appendToElement(root,"short_name",hm.get("SHORT_NAME"));
        appendToElement(root,"code",hm.get("CODE"));


        CreditorDoc[] cd = (CreditorDoc []) hm.get("docs");
        Element docs = getDocument().createElement("docs");
        root.appendChild(docs);

        for(int i=0;i<cd.length;i++)
        {
            Element item = getDocument().createElement("item");
            docs.appendChild(item);
            cd[i].buildElement(item);
        }

        Element subjectType = getDocument().createElement("subject_type");
        root.appendChild(subjectType);

        SubjectType st = (SubjectType) hm.get("subject_type");
        st.buildElement(subjectType);

        if(hm.get("nokbdb") !=null ){
            appendToElement(root, "nokbdb_code" , hm.get("nokbdb").toString() );
            /*Element nokbdb = getDocument().createElement("nokbdb");
            root.appendChild(nokbdb);

            Nokbdb no = (Nokbdb) hm.get("nokbdb");
            no.buildElement(nokbdb);*/
        }

    }

    @Override
    public String asXml(int shft) {
        currentXML = new StringBuilder();
        appendToXml("<name>","</name>",(String)hm.get("NAME"),shft);
        appendToXml("<short_name>","</short_name>",(String)hm.get("SHORT_NAME"),shft);

        StringBuilder sb = new StringBuilder();
        CreditorDoc [] cd = (CreditorDoc []) hm.get("docs");

        for(int i=0;i<cd.length;i++)
            sb.append(indend(shft + 1) + "<item>\n" + cd[i].asXml(shft + 2) + indend(shft + 1) + "</item>\n");

        appendToXml2("<docs>","</docs>",sb.toString(),shft);

        appendToXml2("<subject_type>", "</subject_type>", ((SubjectType) hm.get("subject_type")).asXml(shft + 1), shft);

        return currentXML.toString();

    }

    public void print(int shft) {
        for (Object k : hm.keySet()){
            if(((String)k).equals("docs"))
            {
                CreditorDoc[] cd = (CreditorDoc[]) hm.get("docs");
                for(int i=0;i<cd.length;i++)
                    cd[i].print(shft+1);
            }else
            if(((String)k).equals("subject_type")){
               ((SubjectType) hm.get(k)).print(shft + 1);
            } else  {
            indent(shft); System.out.println(k+" "+hm.get(k));
            }
        }
    }
}
