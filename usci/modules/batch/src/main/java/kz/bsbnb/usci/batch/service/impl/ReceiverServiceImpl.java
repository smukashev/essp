package kz.bsbnb.usci.batch.service.impl;

import kz.bsbnb.usci.batch.service.IReceiverService;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author k.tulbassiyev
 */
@WebService(endpointInterface = "kz.bsbnb.usci.batch.service.IReceiverService")
public class ReceiverServiceImpl implements IReceiverService {
    @WebMethod
    @Override
    public long processBatch(byte[] bytes) {
        return 0;
    }

    @WebMethod
    @Override
    public int test(int a, int b) {
        return a + b;
    }
}
