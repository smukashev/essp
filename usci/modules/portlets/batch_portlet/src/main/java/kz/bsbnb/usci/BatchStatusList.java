package kz.bsbnb.usci;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.log4j.Logger;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class BatchStatusList
 */
public class BatchStatusList extends MVCPortlet {
	private String viewJSP;
	private CouchbaseClient couchbaseClient;
	private Logger logger = Logger.getLogger(BatchStatusList.class);	
		
	@Override
	public void init() throws PortletException {
		viewJSP = getInitParameter("view-template");
		
		//System.setProperty("viewmode", "production");
        System.setProperty("viewmode", "development");

		ArrayList<URI> nodes = new ArrayList<URI>();
	    nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
	        couchbaseClient = new CouchbaseClient(nodes, "test", "");
	    } catch (Exception e) {
	        logger.error("Error connecting to Couchbase: " + e.getMessage());
	        System.exit(1);
	    }
	}

	@Override
	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {
		View view = couchbaseClient.getView("batch", "batch_statuses");
		Query query = new Query();
		query.setLimit(20);
		
		ViewResponse response = couchbaseClient.query(view, query);
		renderRequest.setAttribute("batch-status-result", response);
			
		getPortletContext().getRequestDispatcher(viewJSP).include(renderRequest, renderResponse);
	}
}
