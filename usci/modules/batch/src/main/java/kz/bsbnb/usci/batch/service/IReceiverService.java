package kz.bsbnb.usci.batch.service;

/**
 * @author k.tulbassiyev
 */
public interface IReceiverService {
    public long processBatch(byte bytes[]);
    public int test(int a, int b);
}
