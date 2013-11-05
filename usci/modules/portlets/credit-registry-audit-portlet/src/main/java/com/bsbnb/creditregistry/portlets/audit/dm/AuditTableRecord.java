/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.creditregistry.portlets.audit.dm;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditTableRecord extends AuditEvent{
    public AuditTableRecord(AuditEvent event) {
        super(event);
    }
    
    public String getUsername() {
        try {
            User user = UserLocalServiceUtil.getUserById(getUserId());
            String firstName = user.getFirstName();
            String middleName = user.getMiddleName();
            String lastName = user.getLastName();
            StringBuilder username = new StringBuilder();
            if (lastName != null && !lastName.isEmpty()) {
                username.append(lastName).append(' ');
            }
            if (firstName != null && !firstName.isEmpty()) {
                username.append(firstName.charAt(0)).append('.');
            }
            if (middleName != null && !middleName.isEmpty()) {
                username.append(middleName.charAt(0)).append('.');
            }
            return username.toString();
        } catch (PortalException pe) {
            return "Unknown";
        } catch (SystemException se) {
            return "Unknown";
        }
    }
    
    public String getActionName() {
        AuditEventKind kind = getKind();
        if(kind==null) return "";
        return kind.getName();
    }
    
    public String getActionCode() {
        AuditEventKind kind = getKind();
        if(kind==null) return "";
        return kind.getCode();
    }
}
