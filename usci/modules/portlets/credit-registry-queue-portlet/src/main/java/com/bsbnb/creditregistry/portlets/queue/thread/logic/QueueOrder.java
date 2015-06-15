package com.bsbnb.creditregistry.portlets.queue.thread.logic;

import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface QueueOrder {

    public QueueFileInfo getNextFile(List<QueueFileInfo> files);
}
