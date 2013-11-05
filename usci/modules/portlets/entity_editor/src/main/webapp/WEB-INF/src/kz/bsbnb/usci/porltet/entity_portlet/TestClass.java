package kz.bsbnb.usci.porltet.entityeditor;

/**
 * Created with IntelliJ IDEA.
 * User: Администратор
 * Date: 02.06.13
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public class TestClass {
    private static TestClass ourInstance = new TestClass();
    private String name;

    public static TestClass getInstance() {
        return ourInstance;
    }

    public TestClass() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
