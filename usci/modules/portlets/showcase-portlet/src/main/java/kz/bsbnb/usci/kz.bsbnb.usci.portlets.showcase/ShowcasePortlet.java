package kz.bsbnb.usci.portlets.showcase;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by almaz on 8/5/14.
 */
public class ShowcasePortlet extends MVCPortlet{

    private RmiProxyFactoryBean showcaseServiceFactoryBean;
    private ShowcaseService showcaseService;
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    public void init() throws PortletException {
        showcaseServiceFactoryBean = new RmiProxyFactoryBean();
        showcaseServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1095/showcaseService");
        showcaseServiceFactoryBean.setServiceInterface(ShowcaseService.class);
        showcaseServiceFactoryBean.afterPropertiesSet();
        showcaseService = (ShowcaseService) showcaseServiceFactoryBean.getObject();
        super.init();
    }

    public void listShowcases(ActionRequest request, ActionResponse response){

        List<ShowcaseHolder> list = showcaseService.list();
        request.setAttribute("showcases", list);
    }

    public void deleteShowcase(ActionRequest request, ActionResponse response){
        listShowcases(request, response);
    }

    public void createShowcase(ActionRequest request, ActionResponse response){
        ShowCase showcase = new ShowCase();

        for(int i = 0; request.getParameter("nameField" + i) != null; i++){

        }

        //TODO: create new showcase

        //showcaseService.add(showcase);
        listShowcases(request, response);
    }

    public void viewShowcase(ActionRequest request, ActionResponse response) throws ParseException {
        Long id = new Long(request.getParameter("showcaseId"));

        ShowCase showcase = showcaseService.load(id);
        request.setAttribute("showcase", showcase);
    }

    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException {

        boolean hasRights = false;
        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(request));
            if(user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                            || role.getName().equals("NationalBankEmployee"))
                        hasRights = true;
                }
            }
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }

        if(!hasRights)
            return;

        Long id = new Long(request.getParameter("showcaseId"));
        int count = Integer.parseInt(request.getParameter("count"));
        int page = Integer.parseInt(request.getParameter("page"));

        PrintWriter writer = response.getWriter();
        List<Map<String, Object>> list = null;
        try {
            list = showcaseService.view(id, (page - 1) * count, count, sdf.parse("01.04.2013"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String result = listToJson(list);

        writer.println("{" +
                "\"result\": " + result +
                ",\"total\": 100}");
    }

    private String listToJson(List<Map<String, Object>> list){
        StringBuilder result = new StringBuilder();
        result.append("[");
        for(Map<String, Object> item : list){
            result.append("{");
            for(String key : item.keySet()){
                result.append("\"" + key + "\":\"" + (item.get(key) != null ? ("" + item.get(key)).replace("\"", "'") : "") + "\",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append("},");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
        return result.toString();
    }
}
