package kz.bsbnb.usci.receiver.service;

/**
 * @author k.tulbassiyev
 */
public interface IBatchProcessService {
    public void processBatch(String fileName, Long userId);
}
