package kz.bsbnb.usci.receiver.service;

/**
 * @author k.tulbassiyev
 */
public interface IBatchReceive {
    public long process(byte[] bytes);
}
