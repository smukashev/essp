package kz.bsbnb.usci.receiver.service;

import kz.bsbnb.usci.tool.status.ReceiverStatus;

/**
 * @author k.tulbassiyev
 */
public interface IBatchProcessService {
    public void processBatch(String fileName, Long userId);
    public void processBatchWithoutUser(String fileName);
    public ReceiverStatus getStatus();
}
