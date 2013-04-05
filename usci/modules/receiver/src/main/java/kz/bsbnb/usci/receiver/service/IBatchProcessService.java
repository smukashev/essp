package kz.bsbnb.usci.receiver.service;

/**
 * @author k.tulbassiyev
 */
public interface IBatchProcessService {
    public long processBatch(String fileName, String creditor, byte bytes[]);
}
