package kz.bsbnb.usci.eav.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maksat on 6/1/15.
 */
public class RefListResponse implements Serializable {

    public RefListResponse(List<Map<String, Object>> data) {
        this.data = data;
    }

    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    public List<Map<String, Object>> getData() {
        return data;
    }
}
