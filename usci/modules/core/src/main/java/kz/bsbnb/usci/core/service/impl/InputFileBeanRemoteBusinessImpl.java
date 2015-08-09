package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.eav.model.json.BatchSign;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InputFileBeanRemoteBusinessImpl implements InputFileBeanRemoteBusiness
{
    private Logger logger = Logger.getLogger(InputFileBeanRemoteBusinessImpl.class);

    @Autowired
    private IBatchService batchService;

    @Override
    public InputFile getInputFileByInputInfo(InputInfo inputInfo)
    {
        // TODO method possibly should be removed

        InputFile inputFile = new InputFile();

        inputFile.setFilePath(inputInfo.getFileName());
        inputFile.setId(1L);

        return null;
    }

    @Override
    public List<InputFile> getFilesForSigning(long userId) {
        List<Batch> batchListToSign = batchService.getBatchListToSign(userId);

        ArrayList<InputFile> files = new ArrayList<InputFile>();

        for (Batch batch : batchListToSign) {
            InputFile inputFile = new InputFile();
            inputFile.setId(batch.getId());
            inputFile.setFilePath(batch.getFileName());
            inputFile.setMd5(batch.getHash());
            files.add(inputFile);
        }

        logger.debug("### " + files.size());

        return files;
    }

    @Override
    public void signFile(long fileId, String sign) {
        batchService.signBatch(fileId, sign);
    }
}
