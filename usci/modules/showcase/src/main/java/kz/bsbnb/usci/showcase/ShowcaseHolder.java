package kz.bsbnb.usci.showcase;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import java.io.Serializable;

public class ShowcaseHolder implements Serializable {
    private ShowCase showCaseMeta;

    public ShowCase getShowCaseMeta() {
        return showCaseMeta;
    }

    public void setShowCaseMeta(ShowCase showCaseMeta) {
        this.showCaseMeta = showCaseMeta;
    }

    public String getRootClassName() {
        return getShowCaseMeta().getActualMeta().getClassName();
    }
}
