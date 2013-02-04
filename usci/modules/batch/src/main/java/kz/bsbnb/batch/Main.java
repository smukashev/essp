package kz.bsbnb.batch;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 *
 * @author k.tulbassiyev
 */
public class Main
{
    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[])
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        SessionFactory sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");

    }
}
