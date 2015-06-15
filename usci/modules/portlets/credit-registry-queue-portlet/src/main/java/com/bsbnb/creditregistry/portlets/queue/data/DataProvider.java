package com.bsbnb.creditregistry.portlets.queue.data;

import kz.bsbnb.usci.cr.model.Creditor;
import java.util.List;

public interface DataProvider {
    List<Creditor> getCreditors(long userId, boolean isAdmin);

    List<QueueFileInfo> getQueue(List<Creditor> creditors);
}
