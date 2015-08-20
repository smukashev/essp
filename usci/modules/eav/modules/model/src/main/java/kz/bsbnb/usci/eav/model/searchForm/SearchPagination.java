package kz.bsbnb.usci.eav.model.searchForm;

public class SearchPagination {
    private final int maxPageSize = 50;
    private int pagesCount;
    private int totalCount;

    public SearchPagination(int count){
        totalCount = count;
        pagesCount = (totalCount + maxPageSize - 1 ) / maxPageSize;
    }

    public String getForm(){
        String prefix = "<ul class='pagination'>";
        String suffix = "</ul>";
        String countDom = "<div>Всего результатов: " + totalCount + "</div>";

        for(int i=1;i<=pagesCount;i++)
            prefix += "<a href='' onclick='search(this);'>" + i + "</a>";

        return prefix + suffix + countDom;
    }
}
