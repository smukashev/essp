package kz.bsbnb.usci.eav.model.searchForm;

import java.io.Serializable;

public class SearchPagination implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int fetchSize = 50;
    private int pagesCount;
    private int totalCount;

    public SearchPagination(int count){
        totalCount = count;
        pagesCount = (totalCount + fetchSize - 1 ) / fetchSize;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        pagesCount = (totalCount + fetchSize - 1 ) / fetchSize;
    }

    public String getForm(){
        String prefix = "<ul class='pagination'>";
        String suffix = "</ul>";
        String countDom = "<div>Всего результатов: " + totalCount + "</div>";

        for(int i=1;i<=pagesCount;i++)
            prefix += "<a href='' onclick='search(this);'>" + i + "</a>";

        return prefix + suffix + countDom;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
