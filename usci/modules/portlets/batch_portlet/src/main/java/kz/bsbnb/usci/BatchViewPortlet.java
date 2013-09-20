package kz.bsbnb.usci;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusArrayJModel;
import org.apache.log4j.Logger;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		
		System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");
		
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
			//Object value = couchbaseClient.get("batch:" + batchId);
            View view = couchbaseClient.getView("batch", "batch");
            Query query = new Query();
            query.setGroup(true);
            query.setGroupLevel(1);
            query.setKey("\"" + batchId + "\"");

            ViewResponse response = couchbaseClient.query(view, query);

            Iterator<ViewRow> rows = response.iterator();

            if(rows.hasNext()) {
                ViewRowReduced viewRowNoDocs = (ViewRowReduced) rows.next();

                System.out.println("==================");
                System.out.println(viewRowNoDocs.getValue());
                System.out.println("==================");

                BatchFullStatusJModel batchFullStatusJModel =
                    gson.fromJson(viewRowNoDocs.getValue(), BatchFullStatusJModel.class);
			

				renderRequest.setAttribute("batch", batchFullStatusJModel);
			}

            //ContractStatusArrayJModel

            view = couchbaseClient.getView("batch", "contract_status");
            query = new Query();
            query.setDescending(true);
            query.setRangeEnd("\"" + batchId + "_0\"");
            query.setRangeStart("\"" + batchId + "_9\"");


            response = couchbaseClient.query(view, query);

            rows = response.iterator();

            ArrayList<ContractStatusArrayJModel> csList = new ArrayList<ContractStatusArrayJModel>();
            while(rows.hasNext()) {
                ViewRow viewRowNoDocs = rows.next();

                System.out.println("==================");
                System.out.println(viewRowNoDocs.getValue());
                System.out.println("==================");

                ContractStatusArrayJModel batchFullStatusJModel =
                        gson.fromJson(viewRowNoDocs.getValue(), ContractStatusArrayJModel.class);

                csList.add(batchFullStatusJModel);
            }

            renderRequest.setAttribute("cStats", csList);
		}
		
		getPortletContext().getRequestDispatcher(viewJSP).include(renderRequest, renderResponse);
	}

    @Override
    public void destroy()
    {
        couchbaseClient.shutdown();
        super.destroy();
    }
}
