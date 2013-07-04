<%@page import="kz.bsbnb.usci.eav.model.json.ContractStatusJModel"%>
<%@page import="kz.bsbnb.usci.eav.model.json.BatchStatusJModel"%>
<%@page import="kz.bsbnb.usci.eav.model.json.BatchFullJModel"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<%
	BatchFullJModel batch = (BatchFullJModel) request.getAttribute("batch");
	
	if(batch != null) {
%>
	<table>
		<tr>
			<td><b>Id: </b></td>
			<td><%=batch.getId()%></td>
		</tr>
		<tr>
			<td><b>FileName: </b></td>
			<td><%=batch.getFileName()%></td>
		</tr>
		<tr>
			<td><b>Received: </b></td>
			<td><%=batch.getReceived()%></td>
		</tr>
		<tr>
			<td>Batch Statuses</td>
			<td>
				<ol>		
				<%
					for(BatchStatusJModel bStatus : batch.getStatus().getBatchStatuses()) {
						out.println("<li>" + bStatus.getReceived() + " : " + bStatus.getProtocol() + "</li>");	
					}
				%>
				</ol>
			</td>
		</tr>
		<tr>
			<td>Contract Statuses</td>
			<td>
				<ol>		
				<%
					for(ContractStatusJModel cStatus : batch.getStatus().getContractStatuses()) {
						out.println("<li>" + cStatus.getReceived() + " : " + cStatus.getIndex() + " : " + 
																				cStatus.getProtocol() + "</li>");	
					}
				%>
				</ol>
			</td>
		</tr>
	</table>
<%		
	} else {
		out.println("Batch is NULL!");
	}
%>
