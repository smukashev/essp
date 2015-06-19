package kz.bsbnb.usci.core.service.impl;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.eav.model.json.BatchSign;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class InputFileBeanRemoteBusinessImpl implements InputFileBeanRemoteBusiness
{
    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(InputFileBeanRemoteBusinessImpl.class);

    @PostConstruct
    public void init() {
        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://172.17.110.92:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.error("Error connecting to Couchbase: " + e.getMessage());
        }
    }

    @Override
    public InputFile getInputFileByInputInfo(InputInfo inputInfo)
    {
        InputFile inputFile = new InputFile();

        inputFile.setFilePath(inputInfo.getFileName());
        inputFile.setId(1L);

        return null;
    }

    @Override
    public List<InputFile> getFilesForSigning(long userId)
    {
        Gson gson = new Gson();

        View view = couchbaseClient.getView("batch", "batch_sign");
        Query query = new Query();
        query.setKey("" + userId);

        ViewResponse response = couchbaseClient.query(view, query);

        Iterator<ViewRow> rows = response.iterator();

        ArrayList<InputFile> files = new ArrayList<InputFile>();

        while(rows.hasNext()) {
            ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) rows.next();

            System.out.println("==================");
            System.out.println(viewRowNoDocs.getValue());
            System.out.println("==================");

            BatchSign batchSign =
                    gson.fromJson(viewRowNoDocs.getValue(), BatchSign.class);

            InputFile inputFile = new InputFile();

            inputFile.setId(batchSign.getBatchId());
            inputFile.setFilePath(batchSign.getFileName());
            inputFile.setMd5(batchSign.getMd5());

            files.add(inputFile);
        }

        logger.debug("### " + files.size());

        return files;
    }

    @Override
    public void signFile(long fileId, String sign)
    {
        Gson gson = new Gson();

        BatchSign batchSign = gson.fromJson(couchbaseClient.get("sign:" + fileId).toString(), BatchSign.class);

        if (batchSign != null) {
            batchSign.setSign(sign);
        }

        OperationFuture<Boolean> result2 = couchbaseClient.set("sign:" + fileId, 0, gson.toJson(batchSign));

        while(true) if(result2.isDone()) break; // must be completed
    }
}
