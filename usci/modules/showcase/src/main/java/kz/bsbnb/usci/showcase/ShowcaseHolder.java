package kz.bsbnb.usci.showcase;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowcaseHolder
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

    /**
     * fills prefixToColumn = generates all possible columns from all carteage paths
     */
    public Map<String, String> generatePaths(){
        //if(prefixToColumn != null) return prefixToColumn;

        prefixToColumn = new HashMap<String, String>();
        Map<String,Integer> nextNumber = new HashMap<String,Integer>();
        Map<String,String> columnToPrefix = new HashMap<String, String>();

        for(ShowCaseField field : showCaseMeta.getFieldsList()){
            String pt = "root." + field.getAttributePath();
            String[] temp  = pt.split("\\.");
            String prefix = "";
            for(int i=0;i<temp.length;i++){
                if(temp[i].matches(".*\\d$"))
                    throw new IllegalArgumentException("Description ends with number !!!");
                prefix = prefix.equals("") ? temp[i] : prefix + "." + temp[i];
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
