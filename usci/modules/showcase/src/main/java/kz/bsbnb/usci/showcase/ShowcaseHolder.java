package kz.bsbnb.usci.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowcaseHolder implements Serializable
{
    private ShowCase showCaseMeta;
    private ArrayList<String> idxPaths = new ArrayList<String>();


    /**
     * Map that maps path to corresponding column in table <br/>
     * helps to create Tables and carteage Maps <br/>
     * if path has several appearance it appends number i.e: <br/>
     *
     * <b>Example: </b> <br/>
     * credit: <br/>
     * &nbsp; person<br/>
     * &nbsp;&nbsp; country<br/>
     * &nbsp;&nbsp;&nbsp; name_ru<br/>
     * &nbsp;   organization<br/>
     * &nbsp;&nbsp;      country<br/>
     * &nbsp;&nbsp;&nbsp; name_ru<br/>
     *
     * prefixToColumn will keep as : <br/>
     * ... <br/>
     * credit.person: person<br/>
     * credit.organization: organization<br/>
     * credit.person.country: country1:<br/>
     * credit.organization.country country2: <br/>
     * ...
     */
    private Map<String,String> prefixToColumn;

    public ShowcaseHolder(){}

    public ShowcaseHolder(ShowCase showCaseMeta)
    {
        this.showCaseMeta = showCaseMeta;
    }

    public ShowCase getShowCaseMeta()
    {
        return showCaseMeta;
    }

    public void setShowCaseMeta(ShowCase showCaseMeta)
    {
        this.showCaseMeta = showCaseMeta;
    }

    public String getRootClassName(){
        return getShowCaseMeta().getActualMeta().getClassName();
    }


    /**
     * fills prefixToColumn = generates all possible columns from all carteage paths
     */
    public Map<String, String> generatePaths(){
        //if(prefixToColumn != null) return prefixToColumn;

        prefixToColumn = new HashMap<String, String>();
        Map<String,Integer> nextNumber = new HashMap<String,Integer>();
        Map<String,String> columnToPrefix = new HashMap<String, String>();
        MetaClass metaClass = showCaseMeta.getActualMeta();
        prefixToColumn.put("root",getRootClassName());
        columnToPrefix.put(getRootClassName(), "root");

        for(ShowCaseField field : showCaseMeta.getFieldsList()){

            if(field.getAttributePath().equals(""))
                continue;

            String pt = field.getAttributePath();
            String[] temp  = pt.split("\\.");
            String prefix = "root";
            String path = "";


            for(int i=0;i<temp.length;i++){
                if(temp[i].matches(".*\\d$"))
                    throw new IllegalArgumentException("Description ends with number !!!");
                prefix =  prefix + "." + temp[i];
                if(prefix.startsWith("root."))
                    path = prefix.substring(5);

                IMetaAttribute attribute = metaClass.getMetaAttribute(path);

                if(attribute!=null && attribute.getMetaType().isSet()){
                    MetaSet metaSet = (MetaSet) attribute.getMetaType();
                    temp[i] = ((MetaClass) metaSet.getMemberType() ).getClassName();
                }
                if( !prefixToColumn.containsKey(prefix)){
                    if(!nextNumber.containsKey(temp[i]))
                        nextNumber.put(temp[i], 1);
                    int number = nextNumber.get(temp[i]);
                    nextNumber.put(temp[i], number + 1);
                    prefixToColumn.put(prefix, temp[i] + number);
                    columnToPrefix.put(temp[i] + number, prefix);
                }
            }
        }

        for( String s : nextNumber.keySet()) {
            if( nextNumber.get(s) == 2)
            {
                String prefix = columnToPrefix.get(s + 1);
                prefixToColumn.put( prefix, s);
            }
        }
        return prefixToColumn;
    }
}
