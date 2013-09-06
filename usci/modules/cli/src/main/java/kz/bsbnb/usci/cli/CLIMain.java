package kz.bsbnb.usci.cli;

import kz.bsbnb.usci.cli.app.CLI;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RMISecurityManager;

public class CLIMain
{
    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        System.out.println("Started.");

        CLI app = ctx.getBean(CLI.class);

        app.run();
    }
}
