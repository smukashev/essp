package com.bsbnb.creditregistry.portlets.crosscheck.api;

import com.bsbnb.creditregistry.portlets.crosscheck.model.CrossCheck;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface CrossCheckBeanRemoteBusiness {
    public List<CrossCheck> loadCrossCheck(List<BigInteger> creditorIds, Date date);
}
