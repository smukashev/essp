package kz.bsbnb.usci.eav.util;

import java.io.Serializable;

public class Pair implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String title;

    public Pair(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Pair(long id, String name, String title) {
        this.id = id;
        this.name = name;
        this.title = title;
    }

    public Pair(long id, String title, boolean another) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
