package com.bsbnb.creditregistry.portlets.queue.thread;

import com.bsbnb.creditregistry.dm.ref.shared.InputInfoStatus;
import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrder;
import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrderType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueueHandler implements Runnable {

    public static final int SAME_FILE_SENDING_INTERVAL = 600000;
    private QueueConfiguration configuration;
    private long threadStartTimeMillis = -1;
    private DataProvider dataProvider;
    private QueueOrderType previousOrderType = null;
    private QueueOrder order;
    //Хранится время последней отправки для каждого InputInfo
    //Один и тот же файл не должен быть отправлен дважды в течение 10 минут
    private Map<Integer, Long> lastSentTimeForInputInfoId = new HashMap<Integer, Long>();

    public QueueHandler() {
        dataProvider = new BeanDataProvider();
        configuration = new DatabaseQueueConfiguration(dataProvider);
    }

    public QueueHandler(DataProvider dataProvider, QueueConfiguration configuration) {
        this.dataProvider = dataProvider;
        this.configuration = configuration;
    }

    public void createNewThread() throws ConfigurationException {
        threadStartTimeMillis = System.currentTimeMillis();
        log.log(Level.INFO, "New thread started: {0}", threadStartTimeMillis);
        Thread thread = new Thread(this);
        thread.setName("queue-handler-thread" + threadStartTimeMillis);
        thread.setDaemon(true);
        thread.start();
    }

    /*
     * Определение файла, идущего на обработку
     */
    public QueueFileInfo getNextFile(List<QueueFileInfo> files) {
        //Валидация: удаляем отправки без привязанных файлов
        List<QueueFileInfo> validFiles = new ArrayList<QueueFileInfo>();
        for (QueueFileInfo file : files) {
            if (file.hasFile()) {
                validFiles.add(file);
            }
        }

        //Сначала определяем первые файлы по каждому кредитору
        List<QueueFileInfo> firstFilesByEachCreditor = new ArrayList<QueueFileInfo>();

        Set<Integer> creditorIds = new HashSet<Integer>();
        for (QueueFileInfo file : validFiles) {
            if (!creditorIds.contains(file.getCreditorId())) {
                firstFilesByEachCreditor.add(file);
            }
            creditorIds.add(file.getCreditorId());
        }

        //Из них выбираем те, которые не обрабатываются в данный момент
        List<QueueFileInfo> nonProcessingFiles = new ArrayList<QueueFileInfo>(firstFilesByEachCreditor.size());
        for (QueueFileInfo file : firstFilesByEachCreditor) {
            if (!InputInfoStatus.BATCH_PROCESSING_IN_PROGRESS.getCode().equals(file.getStatusCode())) {
                nonProcessingFiles.add(file);
            }
        }

        //Из них выбираем те файлы, которые не отправлялись на обработку в течение заданного интервала
        List<QueueFileInfo> filesNotSentRecently = new ArrayList<QueueFileInfo>(nonProcessingFiles.size());
        for (QueueFileInfo file : nonProcessingFiles) {
            if (lastSentTimeForInputInfoId.containsKey(file.getInputInfoId())) {
                long lastTimeSent = lastSentTimeForInputInfoId.get(file.getInputInfoId());
                if (System.currentTimeMillis() - lastTimeSent > SAME_FILE_SENDING_INTERVAL) {
                    filesNotSentRecently.add(file);
                }
            } else {
                filesNotSentRecently.add(file);
            }
        }

        //Из полученных выбираем файлы приоритетных кредиторов
        List<QueueFileInfo> priorityCreditorsFiles = new ArrayList<QueueFileInfo>(filesNotSentRecently.size());
        Set<Integer> priorityCreditorIds = new HashSet<Integer>();
        for (int creditorId : configuration.getPriorityCreditorIds()) {
            priorityCreditorIds.add(creditorId);
        }
        for (QueueFileInfo file : filesNotSentRecently) {
            if (priorityCreditorIds.contains(file.getCreditorId())) {
                priorityCreditorsFiles.add(file);
            }
        }
        //если таковых нет, то пропускаем всех остальных
        if (priorityCreditorsFiles.isEmpty()) {
            priorityCreditorsFiles = filesNotSentRecently;
        }

        //Далее работает текущая логика
        QueueOrderType newOrderType = configuration.getOrderType();
        if (newOrderType != previousOrderType) {
            previousOrderType = newOrderType;
            order = newOrderType.createOrder();
        }
        return order.getNextFile(priorityCreditorsFiles);
    }

    public List<QueueFileInfo> getOrderedFiles(List<QueueFileInfo> files) {
        List<QueueFileInfo> orderedFiles = new ArrayList<QueueFileInfo>(files.size());
        List<QueueFileInfo> tempFiles = new ArrayList<QueueFileInfo>();
        for (QueueFileInfo file : files) {
            if (file != null && !InputInfoStatus.BATCH_PROCESSING_IN_PROGRESS.getCode().equals(file.getStatusCode())) {
                tempFiles.add(file);
            }
        }
        int numberOfFiles = tempFiles.size();
        for (int i = 0; i < numberOfFiles; i++) {
            QueueFileInfo nextFile = getNextFile(tempFiles);
            orderedFiles.add(nextFile);
            tempFiles.remove(nextFile);
        }
        return orderedFiles;
    }

    private void checkStatus() {
        try {
            if (!isCounterValid()) {
                log.log(Level.INFO, "Skip because of counter");
                return;
            }
            List<QueueFileInfo> files = dataProvider.getQueue(null);
            log.log(Level.INFO, "Files in queue: {0}", files.size());
            ReceiveCredits port = getServicePort(configuration.getWsdlLocation());
            if (port == null) {
                return;
            }
            QueueFileInfo nextFileInfo = getNextFile(files);
            if (nextFileInfo != null) {
                log.log(Level.INFO, "File path: {0}", nextFileInfo.getFilePath());
                File file = new File(nextFileInfo.getFilePath());
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    if (file.length() > Integer.MAX_VALUE) {
                        log.log(Level.SEVERE, "File too large: {0}", nextFileInfo.getFilePath());
                    } else {
                        byte[] buf = new byte[(int) file.length()];
                        fis.read(buf);
                        log.log(Level.INFO, "Input info ID: {0}", nextFileInfo.getInputInfoId());
                        log.log(Level.INFO, "Number of bytes: {0}", buf.length);
                        boolean toSend = true;
                        if (lastSentTimeForInputInfoId.containsKey(nextFileInfo.getInputInfoId())) {
                            long lastTimeSent = lastSentTimeForInputInfoId.get(nextFileInfo.getInputInfoId());
                            toSend = System.currentTimeMillis() - lastTimeSent > SAME_FILE_SENDING_INTERVAL;
                        }
                        if (toSend) {
                            lastSentTimeForInputInfoId.put(nextFileInfo.getInputInfoId(), System.currentTimeMillis());
                            port.receiveZippedXmlWithoutInputInfo(nextFileInfo.getInputInfoId(), buf, nextFileInfo.getFilePath(), (long) nextFileInfo.getUserId());
                            log.log(Level.INFO, "File sent");
                        } else {
                            log.log(Level.INFO, "Same file send attempt");
                        }
                    }
                } catch (FileNotFoundException fnfe) {
                    log.log(Level.SEVERE, "File not found: {0}", nextFileInfo.getFilePath());
                } catch (IOException ioe) {
                    log.log(Level.SEVERE, "Error reading file: {0}", nextFileInfo.getFilePath());
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e) {
                        }
                    }
                }
            } else {
                log.log(Level.INFO, "No input infos");
            }
        } catch (ConfigurationException ce) {
            log.log(Level.WARNING, "Configuration fail", ce);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Unexpected exception. ", t);
        }
    }

    private boolean isCounterValid() throws ConfigurationException {
        return configuration.getFilesInProcessing() < configuration.getParallelLimit();
    }

    private ReceiveCredits getServicePort(final String wsdlLocation) {
        try {
            if (wsdlLocation == null) {
                log.log(Level.WARNING, "WSDL Location is null");
                return null;
            }
            final CreditReceiver service = new CreditReceiver(new URL(wsdlLocation));
            return service.getCreditReceiverPort();
        } catch (WebServiceException serviceError) {
            log.log(Level.SEVERE, "Failed to access service with message: {0}", serviceError.getMessage());
        } catch (MalformedURLException mue) {
            log.log(Level.SEVERE, "Bad wsdl location: {0}", mue.getMessage());
        }
        return null;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
            while (true) {
                try {
                    configuration.setLastLaunchMillis(threadStartTimeMillis);
                    break;
                } catch (ConfigurationException ce) {
                    log.log(Level.WARNING, "", ce);
                    Thread.sleep(10000);
                }
            }
            while (true) {
                try {
                    Long launchTimeMillis = null;
                    try {
                        launchTimeMillis = configuration.getLastLaunchMillis();
                    } catch (ConfigurationException ce) {
                        log.log(Level.WARNING, "", ce);
                    }
                    if (launchTimeMillis != null) {
                        if (launchTimeMillis != threadStartTimeMillis) {
                            log.log(Level.WARNING, "Last launch time doesn't match");
                            break;
                        }
                        checkStatus();
                    } else {
                        log.log(Level.WARNING, "Last launch time not found");
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Unexpected exception", ex);
                }
                Thread.sleep(10000);
            }

        } catch (InterruptedException ie) {
            log.log(Level.WARNING, "Thread sleep fail", ie);
        }
        try {
            configuration.setLastLaunchMillis(-1);
        } catch (ConfigurationException ce) {
            log.log(Level.INFO, "", ce);
        } catch (Exception ex) {
            log.log(Level.INFO, "Unexpected exception", ex);
        }

        log.log(Level.INFO, "Thread finished");
    }
}
