<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.couchbase.client.protocol.views.ViewResponse" %>
<%@ page import="com.couchbase.client.protocol.views.ViewRow" %>
<%@ page import="com.couchbase.client.protocol.views.ViewRowNoDocs" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="kz.bsbnb.usci.eav.model.json.BatchFullJModel" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<h2 align="center">Batches</h2>
<table style="width:100%; border: 1px solid #000;" border="1">
<thead>
	<tr>
		<th>ID</th>
		<th>File name</th>
		<th>Status</th>
	</tr>
</thead>
<tbody>

<%
	Gson gson = new Gson();
	ViewResponse viewResponse = (ViewResponse) request.getAttribute("batch-status-result");
	String line = "";
	
	if(viewResponse != null) {	
		for(ViewRow row : viewResponse) {
			line += "<tr>";
			ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) row;	
			
			List list = gson.fromJson(viewRowNoDocs.getValue(), ArrayList.class);
			
			Double dId = Double.parseDouble(list.get(0).toString());
			Long id = dId.longValue();
			
			line += "<td>" + id + "</td>";
			line += "<td><a href='/web/guest/who-is-using-liferay?batchId=" + id + "'>" + list.get(1) + "</a></td>";
			line += "<td>" + list.get(2) + "</td>";
			
			line += "</tr>";
		}
	}
	
	out.println(line);
%>

</tbody>
</table>
