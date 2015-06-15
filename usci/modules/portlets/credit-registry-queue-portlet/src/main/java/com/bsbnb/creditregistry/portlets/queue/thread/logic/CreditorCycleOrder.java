package com.bsbnb.creditregistry.portlets.queue.thread.logic;

import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Отправляется файл того кредитора, который не обрабатывался дольше всех. 
 * Т.о. если в очереди находятся файлы от N кредиторов, то 
 * среди следующих N файлов будет по одному от каждого из них.
 *
 * @author Aidar.Myrzahanov
 */
class CreditorCycleOrder implements QueueOrder {

    /*
     * В индексе хранится соответствие ID кредитора количеству файлов других организаций
     * которые были отправлены после последней отправки файла этого кредитора.
     * 
     * Если были последовательно выбраны файлы кредиторов с ID 1, 2, и 3, то 
     * ожидаемое состояние индекса: {{1=3},{2=2},{3=1}}
     */
    private Map<Integer, Integer> creditorIndex = new HashMap<Integer, Integer>();

    public QueueFileInfo getNextFile(List<QueueFileInfo> files) {
        QueueFileInfo nextFile = null;
        for (QueueFileInfo file : files) {
            if (nextFile == null) {
                //Кандидат еще не определился выбираем первый попавшийся
                nextFile = file;
            } else if (creditorIndex.containsKey(nextFile.getCreditorId())) {
                //Кредитор файла-кандидата уже проходил обработку
                if (!creditorIndex.containsKey(file.getCreditorId())) {
                    //А кредитор текущего файла ее не проходил и потому имеет преимущество
                    nextFile = file;
                } else {
                    //Если и файл-кандидат, и текущий файл отосланы кредиторами,
                    //которые уже проходили обработку, то оцениваем количество
                    //файлов, которые были отправлены в обработку с момента последней 
                    //обработки каждого из них. Выбираем тот, который ожидал больше.
                    int currentValue = creditorIndex.get(nextFile.getCreditorId());
                    int newValue = creditorIndex.get(file.getCreditorId());
                    if (newValue > currentValue) {
                        nextFile = file;
                    }
                }
            }
        }
        if (nextFile != null) {
            //Обновляем индекс по выбранному кредитору, он обработался на текущем шаге
            creditorIndex.put(nextFile.getCreditorId(), 0);
        }
        //Увеличиваем количество шагов для каждого кредитора
        Map<Integer, Integer> newCreditorIndex = new HashMap<Integer, Integer>();
        for (Entry<Integer, Integer> entry : creditorIndex.entrySet()) {
            newCreditorIndex.put(entry.getKey(), entry.getValue() + 1);
        }
        creditorIndex = newCreditorIndex;
        return nextFile;
    }
}
