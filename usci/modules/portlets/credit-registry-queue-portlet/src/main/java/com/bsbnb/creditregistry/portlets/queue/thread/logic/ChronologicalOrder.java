package com.bsbnb.creditregistry.portlets.queue.thread.logic;

import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
class ChronologicalOrder implements QueueOrder {

    @Override
    public QueueFileInfo getNextFile(List<QueueFileInfo> files) {
        QueueFileInfo result = null;
        for (QueueFileInfo file : files) {
            if (result == null || result.getInputInfoId() > file.getInputInfoId()) {
                result = file;
            }
        }
        return result;
    }
}
