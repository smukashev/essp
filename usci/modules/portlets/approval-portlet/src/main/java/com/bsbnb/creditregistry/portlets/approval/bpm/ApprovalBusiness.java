package com.bsbnb.creditregistry.portlets.approval.bpm;

import kz.bsbnb.usci.eav.util.Errors;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nurlan on 3/30/15.
 */
public class ApprovalBusiness {

    private static final String BONITA_URI = "http://localhost:8082/bonita";

    /**
     * Human user username
     */
    private static final String USERNAME = "walter.bates";

    /**
     * Human user password
     */
    private static final String PASSWORD = "bpm";

    private static final String PROCESS_NAME = "ApprovalPool";
    private Long processDefinitionId;

    private final HttpClient httpClient;
    private HttpContext httpContext;

    private final String bonitaURI;

    public ApprovalBusiness() {
        PoolingClientConnectionManager conMan = getConnectionManager();
        this.httpClient = new DefaultHttpClient(conMan);
        this.bonitaURI = BONITA_URI;
    }

    public void startApprovalProcess(Map<String, String> data) {
        loginAs(USERNAME, PASSWORD);
        this.processDefinitionId = getProcessId(PROCESS_NAME);
        startACase(processDefinitionId, data);
        logout();
    }

    public int startACase(long processDefinitionId, Map<String, String> data) {
        System.out.println("Starting a new case of process " + PROCESS_NAME + " (ID: " + processDefinitionId + ").");
        String apiURI = "/API/bpm/case/";
        String payloadAsString = "{\"processDefinitionId\": " + processDefinitionId + ", ";
        payloadAsString += "\"variables\": [ ";
        boolean isFirst = true;
        for (String key : data.keySet()) {
            if (!isFirst) {
                payloadAsString += ",";
            }
            String value = data.get(key);
            payloadAsString += "{\"name\": \"" + key + "\", \"value\": \"" + value + "\"}";
            isFirst = false;
        }
        payloadAsString += "]}";

        return consumeResponse(executePostRequest(apiURI, payloadAsString),true);
    }

    public void close() {
        httpClient.getConnectionManager().shutdown();
    }

    private static PoolingClientConnectionManager getConnectionManager() {
        PoolingClientConnectionManager conMan = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault());
        conMan.setMaxTotal(200);
        conMan.setDefaultMaxPerRoute(200);
        return conMan;
    }

    private int executePostRequest(String apiURI, UrlEncodedFormEntity entity) {
        try {
            HttpPost postRequest = new HttpPost(bonitaURI + apiURI);
            postRequest.setEntity(entity);
            HttpResponse response = httpClient.execute(postRequest, httpContext);
            return consumeResponse(response, true);
        } catch (HttpHostConnectException e) {
            throw new RuntimeException(Errors.getMessage(Errors.E203,BONITA_URI,e));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private HttpResponse executePostRequest(String apiURI, String payloadAsString) {
        try {
            HttpPost postRequest = new HttpPost(bonitaURI + apiURI);
            StringEntity input = new StringEntity(payloadAsString, "utf-8");
            input.setContentType("application/json");
            postRequest.setEntity(input);
            HttpResponse response = httpClient.execute(postRequest, httpContext);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private int consumeResponse(HttpResponse response, boolean printResponse) {
        String responseAsString = consumeResponseIfNecessary(response);
        if(printResponse) {
            System.out.println(responseAsString);
        }
        return ensureStatusOk(response);
    }

    private String consumeResponseIfNecessary(HttpResponse response) {
        if (response.getEntity() != null) {
            BufferedReader rd;
            try {
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                throw new RuntimeException(Errors.getMessage(Errors.E201), e);
            }
        } else {
            return "";
        }
    }

    private int ensureStatusOk(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException(Errors.getMessage(Errors.E202,response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase()));
        }
        return response.getStatusLine().getStatusCode();
    }

    public Long getProcessId(String serviceName) {
        String serviceUri = "/API/bpm/process?f=activationState%3dENABLED&f=name%3d" + serviceName;
        HttpResponse response = executeGetRequest(serviceUri);
        String content = consumeResponseIfNecessary(response);
        /*content = content.substring(8, content.indexOf(',') - 1);*/

        int s1 = content.indexOf("\"id\"");
        int s2 = content.indexOf(",", s1);
        content = content.substring(s1 + 6, s2 - 1);

        Long processId = null;

        try {
            processId = Long.parseLong(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return processId;
    }

    public void loginAs(String username, String password) {
        try {
            CookieStore cookieStore = new BasicCookieStore();
            httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            String loginURL = "/loginservice";
            // If you misspell a parameter you will get a HTTP 500 error
            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("username", username));
            urlParameters.add(new BasicNameValuePair("password", password));
            urlParameters.add(new BasicNameValuePair("redirect", "false"));
            // UTF-8 is mandatory otherwise you get a NPE
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(urlParameters, "utf-8");
            executePostRequest(loginURL, entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void logout() {
        consumeResponse(executeGetRequest("/logoutservice"),false);
    }

    private HttpResponse executeGetRequest(String apiURI) {
        try {
            HttpGet getRequest = new HttpGet(bonitaURI + apiURI);
            HttpResponse response = httpClient.execute(getRequest, httpContext);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
