package com.bsbnb.creditregistry.portlets.administration;

import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.log4j.Logger;

import com.bsbnb.creditregistry.portlets.administration.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.administration.ui.MainSplitPanel;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.*;

/** 
 *
 * @author Marat Madybayev
 * @version 
 */
public class AdministrationApplication extends Application implements PortletRequestListener {

    private ResourceBundle bundle;
    public static final org.apache.log4j.Logger log = Logger.getLogger(AdministrationApplication.class.getName());

    @Override
    public void init() {
        Window mainWindow = new Window(bundle.getString("WindowsTitle"));
        BeanDataProvider provider = new BeanDataProvider();
        MainSplitPanel sp = new MainSplitPanel(bundle,provider);
        mainWindow.addComponent(sp);
        setMainWindow(mainWindow);
    }

    public void onRequestStart(PortletRequest request, PortletResponse response) {
        bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
    }

    public void onRequestEnd(PortletRequest request, PortletResponse response) {
    }
}
