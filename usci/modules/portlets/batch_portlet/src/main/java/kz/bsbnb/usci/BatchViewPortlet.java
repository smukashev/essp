package kz.bsbnb.usci;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import kz.bsbnb.usci.eav.model.json.BatchFullJModel;

import org.apache.log4j.Logger;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class BatchViewPortlet
 */
public class BatchViewPortlet extends MVCPortlet {
	private String viewJSP;
	private CouchbaseClient couchbaseClient;
	private Gson gson = new Gson();
	
	private Logger logger = Logger.getLogger(BatchViewPortlet.class);
	
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
	        logger.info("Error connecting to Couchbase: " + e.getMessage());
	        System.exit(1);
	    }
	}

	@Override
	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {	
		HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(
				PortalUtil.getHttpServletRequest(renderRequest));
		
		String batchId = httpReq.getParameter("batchId");
		
		if(batchId != null) {
			Object value = couchbaseClient.get("batch:" + batchId);	
			
			if(value != null) {
				BatchFullJModel batch = gson.fromJson(value.toString(), BatchFullJModel.class);
				renderRequest.setAttribute("batch", batch);
			}
		}
		
		getPortletContext().getRequestDispatcher(viewJSP).include(renderRequest, renderResponse);
	}
}
