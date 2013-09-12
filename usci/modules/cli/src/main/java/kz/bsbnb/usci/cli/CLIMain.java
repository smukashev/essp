package kz.bsbnb.usci.cli;

import kz.bsbnb.usci.cli.app.CLI;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.RMISecurityManager;

/**
 * clear
 * init
 * xsd convert path_to_file ct_package
 * meta show name person | ct_package
 * crbatch path_to_xml count
 */

public class CLIMain
{
    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        System.out.println("Started.");

        CLI app = ctx.getBean(CLI.class);

        if (args.length > 0) {
            File f = new File(args[0]);

            try
            {
                InputStream inputStream = new FileInputStream(f);
                app.setInputStream(inputStream);
                System.out.println("Using file: " + args[0]);
            } catch (FileNotFoundException e)
            {
                System.out.println("Error file: " + args[0] + " does not exist.");
                System.out.println(f.getAbsoluteFile());
            }
        }

        app.run();
    }
}
