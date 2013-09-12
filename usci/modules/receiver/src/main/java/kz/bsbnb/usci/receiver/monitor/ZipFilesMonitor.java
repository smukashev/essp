package kz.bsbnb.usci.receiver.monitor;

import kz.bsbnb.usci.receiver.entry.BatchInfo;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author abukabayev
 */
public class ZipFilesMonitor{

    public void readFiles(String filename){
        BatchInfo batchInfo = new BatchInfo();
        try{

            ZipFile zipFile = new ZipFile(filename);

            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");
            ZipEntry dataEntry = zipFile.getEntry("data.xml");

            InputStream inManifest = zipFile.getInputStream(manifestEntry);
            InputStream inData = zipFile.getInputStream(dataEntry);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            Document document = null;
            try {
                document = documentBuilder.parse(inManifest);
            } catch (SAXException e) {
                e.printStackTrace();
            }


            batchInfo.setBatchType(document.getElementsByTagName("type").item(0).getTextContent());
            batchInfo.setBatchName(document.getElementsByTagName("name").item(0).getTextContent());
            batchInfo.setUserId(Long.parseLong(document.getElementsByTagName("userid").item(0).getTextContent()));
            batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));


            Date date = null;
            try {
                date = new SimpleDateFormat("dd.MM.yy").parse(document.getElementsByTagName("date").item(0).getTextContent());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            batchInfo.setRepDate(date);

            System.out.println(batchInfo.getSize());
            System.out.println(batchInfo.getRepDate());


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void monitor(Path path) throws InterruptedException, IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        boolean valid = true;
        do {
            WatchKey watchKey = watchService.take();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("File Created:" + fileName);

                    readFiles(path+"/"+fileName);



                }
            }
            valid = watchKey.reset();

        } while (valid);

    }
}
