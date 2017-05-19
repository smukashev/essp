package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;

import java.util.Date;
import java.util.List;

public interface InputFileBeanRemoteBusiness {
    InputFile getInputFileByInputInfo(InputInfo inputInfo);

    List<InputFile> getFilesForSigning(long creditorId);

    void signFile(long fileId, String sign, String signInfo, Date signTime);
}
