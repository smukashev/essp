package com.bsbnb.creditregistry.portlets.crosscheck.api;

import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheck;
import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheckMessage;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface CrossCheckMessageBeanRemoteBusiness {
    public List<CrossCheckMessage> getMessagesByCrossCheck(CrossCheck crossCheck);
}
