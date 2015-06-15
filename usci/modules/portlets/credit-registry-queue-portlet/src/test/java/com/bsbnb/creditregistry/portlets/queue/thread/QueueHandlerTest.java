package com.bsbnb.creditregistry.portlets.queue.thread;

import com.bsbnb.creditregistry.dm.ref.shared.InputInfoStatus;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrderType;
import com.bsbnb.creditregistry.portlets.queue.ui.TestDataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueHandlerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public QueueHandlerTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private QueueFileInfo[] getPermutation(QueueFileInfo[] originalOrder, int[] permutation) {
        QueueFileInfo[] newOrder = new QueueFileInfo[permutation.length];
        for (int i = 0; i < permutation.length; i++) {
            newOrder[i] = originalOrder[permutation[i]];
        }
        return newOrder;
    }

    @Test
    public void testOrder() {
        QueueFileInfo[] files = new QueueFileInfo[6];
        files[0] = QueueFilesFactory.INSTANCE.getNextFile(1, 10000, "first");
        files[1] = QueueFilesFactory.INSTANCE.getNextFile(1, 100, "second");
        files[2] = QueueFilesFactory.INSTANCE.getNextFile(2, 100, "third");
        files[3] = QueueFilesFactory.INSTANCE.getNextFile(3, 1, "fourth");
        files[4] = QueueFilesFactory.INSTANCE.getNextFile(4, 12, InputInfoStatus.BATCH_PROCESSING_IN_PROGRESS.getCode());
        files[5] = QueueFilesFactory.INSTANCE.getNextFile(4, 10, "fifth");

        QueueFileInfo[] chronologicalOrderFiles = getPermutation(files, new int[]{0, 1, 2, 3, 5});
        QueueFileInfo[] fileSizeOrderFiles = getPermutation(files, new int[]{3, 5, 2, 0, 1});
        QueueFileInfo[] secondPriorityOrderFiles = getPermutation(files, new int[]{2, 0, 1, 3, 5});

        QueueConfiguration configuration = new TestQueueConfiguration();
        configuration.setPriorityCreditorIds(new ArrayList<Integer>());
        configuration.setOrderType(QueueOrderType.CHRONOLOGICAL);
        QueueHandler handler = new QueueHandler(new TestDataProvider(), configuration);
        QueueFileInfo[] sortedFiles = handler.getOrderedFiles(Arrays.asList(files)).toArray(new QueueFileInfo[0]);
        assertArrayEquals(chronologicalOrderFiles, sortedFiles);

        configuration.setOrderType(QueueOrderType.MINIMAL_WEIGHT_FIRST);
        assertArrayEquals(fileSizeOrderFiles, handler.getOrderedFiles(Arrays.asList(files)).toArray());

        configuration.setPriorityCreditorIds(Arrays.asList(new Integer[]{2}));
        configuration.setOrderType(QueueOrderType.CHRONOLOGICAL);
        assertArrayEquals(secondPriorityOrderFiles, handler.getOrderedFiles(Arrays.asList(files)).toArray());

        assertEquals(0, handler.getOrderedFiles(new ArrayList<QueueFileInfo>()).size());
    }

    private static enum QueueFilesFactory {

        INSTANCE;
        private static int counter = 1;
        private final Calendar date = Calendar.getInstance();

        public QueueFileInfo getNextFile(int creditorId, long fileSize, String statusCode) {
            QueueFileInfo file = new QueueFileInfo();
            file.setStatusCode(statusCode);
            file.setInputInfoId(counter);
            counter++;
            file.setReceiverDate(date.getTime());
            date.add(Calendar.HOUR, 1);
            file.setCreditorId(creditorId);
            file.setFilePath("");
            file.setLength(fileSize);
            return file;
        }
    }

}
