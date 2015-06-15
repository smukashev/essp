package com.bsbnb.creditregistry.portlets.queue.thread.logic;

import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import java.util.List;

/**
 * Порядок, при котором приоритет имеет файл наименьшего веса
 *
 * @author Aidar.Myrzahanov
 */
class MinimumWeightOrder implements QueueOrder {

    public QueueFileInfo getNextFile(List<QueueFileInfo> files) {
        QueueFileInfo nextFile = null;
        for (QueueFileInfo file : files) {
            if (nextFile == null || nextFile.getLength() > file.getLength()) {
                nextFile = file;
            }
        }
        return nextFile;
    }
}
