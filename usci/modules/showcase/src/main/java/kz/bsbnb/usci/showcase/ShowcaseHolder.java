package kz.bsbnb.usci.showcase;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ShowcaseHolder implements Serializable {
    private ShowCase showCaseMeta;

    public ShowcaseHolder() {
        super();
    }

    public ShowcaseHolder(ShowCase showCaseMeta) {
        this.showCaseMeta = showCaseMeta;
    }

    public ShowCase getShowCaseMeta() {
        return showCaseMeta;
    }

    public void setShowCaseMeta(ShowCase showCaseMeta) {
        this.showCaseMeta = showCaseMeta;
    }

    public String getRootClassName() {
        return getShowCaseMeta().getActualMeta().getClassName();
    }

    public Map<String, String> generatePaths() {
        Map<String, String> prefixToColumn = new HashMap<>();

        Map<String, Integer> nextNumber = new HashMap<>();
        Map<String, String> columnToPrefix = new HashMap<>();

        MetaClass metaClass = showCaseMeta.getActualMeta();

        prefixToColumn.put("root", getRootClassName());
        columnToPrefix.put(getRootClassName(), "root");

        for (ShowCaseField field : showCaseMeta.getFieldsList()) {
            if (field.getAttributePath().equals(""))
                continue;

            String pt = field.getAttributePath();
            String[] temp = pt.split("\\.");
            String prefix = "root";
            String path = "";


            for (int i = 0; i < temp.length; i++) {
                if (temp[i].matches(".*\\d$"))
                    throw new IllegalArgumentException("Description ends with number !!!");

                prefix = prefix + "." + temp[i];

                if (prefix.startsWith("root."))
                    path = prefix.substring(5);

                IMetaAttribute attribute = metaClass.getMetaAttribute(path);

                if (attribute != null && attribute.getMetaType().isSet()) {
                    MetaSet metaSet = (MetaSet) attribute.getMetaType();
                    temp[i] = ((MetaClass) metaSet.getMemberType()).getClassName();
                }

                // Filter prefixes
                boolean hasFilter = false;
                for(ShowCaseField sf : showCaseMeta.getFilterFieldsList()) {
                    if(path.equals(sf.getAttributePath())) {
                        hasFilter = true;
                        break;
                    }
                }

                if (!hasFilter && !prefixToColumn.containsKey(prefix)) {
                    if (!nextNumber.containsKey(temp[i]))
                        nextNumber.put(temp[i], 1);

                    int number = nextNumber.get(temp[i]);
                    nextNumber.put(temp[i], number + 1);

                    prefixToColumn.put(prefix, temp[i] + number);
                    columnToPrefix.put(temp[i] + number, prefix);
                }
            }
        }

        for (String s : nextNumber.keySet()) {
            if (nextNumber.get(s) == 2) {
                String prefix = columnToPrefix.get(s + 1);
                prefixToColumn.put(prefix, s);
            }
        }

        return prefixToColumn;
    }

    @Override
    public String toString() {
        return "ShowcaseHolder{" +
                "showCaseMeta=" + (showCaseMeta == null ? null : showCaseMeta.getTableName()) +
                '}';
    }
}
