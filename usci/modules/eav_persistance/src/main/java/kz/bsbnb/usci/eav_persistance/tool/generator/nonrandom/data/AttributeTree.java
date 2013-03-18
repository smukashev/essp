package kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author abukabayev
 */
public class AttributeTree {
    private List<AttributeTree> Children = new LinkedList<AttributeTree>();
    private AttributeTree parent = null;
    private String name;


    public AttributeTree(String name,AttributeTree parent) {
        this.name = name;
        this.parent = parent;
    }

    public AttributeTree addChild(AttributeTree child){
        this.Children.add(child);
        return child;
    }

    public String getName() {
        return name;
    }

    public List<AttributeTree> getChildren() {
        return Children;
    }

    public Boolean hasChildren(){
        return !this.getChildren().isEmpty();
    }

}