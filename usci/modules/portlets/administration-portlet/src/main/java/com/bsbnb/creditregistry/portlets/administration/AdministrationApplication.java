package com.bsbnb.creditregistry.portlets.administration;

import com.bsbnb.creditregistry.portlets.administration.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.administration.ui.MainSplitPanel;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Window;
import org.apache.log4j.Logger;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ResourceBundle;

/**
 * @author Marat Madybayev
 */
public class AdministrationApplication extends Application implements PortletRequestListener {

    public static final org.apache.log4j.Logger log = Logger.getLogger(AdministrationApplication.class.getName());
    private ResourceBundle bundle;

    @Override
    public void init() {
        Window mainWindow = new Window(bundle.getString("WindowsTitle"));
        BeanDataProvider provider = new BeanDataProvider();
        MainSplitPanel sp = new MainSplitPanel(bundle, provider);
        mainWindow.addComponent(sp);
        setMainWindow(mainWindow);
    }

    public void onRequestStart(PortletRequest request, PortletResponse response) {
        bundle = ResourceBundle.getBundle("content.Language", request.getLocale());
    }

    public void onRequestEnd(PortletRequest request, PortletResponse response) {
    }
}
