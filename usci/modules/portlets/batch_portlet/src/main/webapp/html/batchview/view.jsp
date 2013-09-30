<%@ page import="kz.bsbnb.usci.eav.model.json.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<%
    BatchFullStatusJModel batch = (BatchFullStatusJModel) request.getAttribute("batch");
    ArrayList<ContractStatusArrayJModel> cStats = (ArrayList<ContractStatusArrayJModel>)request.getAttribute("cStats");
	
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

					if (cStats != null) {
                        if (cStats.size() > 0) {
                            for(ContractStatusArrayJModel cStatus : cStats) {
                                for (ContractStatusJModel csajm : cStatus.getContractStatuses()) {
                                    out.println("<li>" + csajm.getReceived() + " : " + csajm.getIndex() + " : " +
                                            csajm.getProtocol() + "</li>");
                                }
                            }
                        } else {
                            out.println("<li>No records</li>");
                        }
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
