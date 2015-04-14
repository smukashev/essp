package kz.bsbnb.usci.showcase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RMISecurityManager;

public class Main {
    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        System.out.println("RMI server started");

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
            System.out.println("Security manager installed.");
        } else {
            System.out.println("Security manager already exists.");
        }
    }
}
