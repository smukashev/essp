package kz.bsbnb.usci.portlet.report.test;

import kz.bsbnb.usci.portlet.report.data.*;

import java.util.List;
import  java.util.*;

/**
 * Created by Bauyrzhan.Ibraimov on 15.06.2015.
 */
public class Test {

    public Test(Date repdate)
    {
        DataProvider provider = new BeanDataProvider();
        List<InputInfoDisplayBean> list  =provider.getInputInfosByCreditors(provider.getCreditorsList(), repdate);
    }
}
