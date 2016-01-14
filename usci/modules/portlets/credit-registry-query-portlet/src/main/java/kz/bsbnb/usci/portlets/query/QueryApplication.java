package kz.bsbnb.usci.portlets.query;

import com.bsbnb.vaadin.base.portlet.BaseApplication;
import com.bsbnb.vaadin.base.portlet.PortletEnvironment;
import com.vaadin.ui.Window;
import java.util.logging.Logger;

public class QueryApplication extends BaseApplication {

    private static final long serialVersionUID = 2096197512742005243L;

    public static final Logger log = Logger.getLogger(QueryApplication.class.getCanonicalName());

    @Override
    protected Window createWindow(PortletEnvironment env) {
        Window mainWindow = new Window();
        if (env.isUserAdmin()) {
            SqlExecutor executor = new SqlExecutor(new QuerySettings(env.getRequest()));
            mainWindow.addComponent(new QueryComponent(executor));
        }
        return mainWindow;
    }

}
