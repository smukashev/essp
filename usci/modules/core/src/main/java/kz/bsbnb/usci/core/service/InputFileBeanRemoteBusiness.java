package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;

import java.util.List;

public interface InputFileBeanRemoteBusiness
{
    public InputFile getInputFileByInputInfo(InputInfo inputInfo);

    public List<InputFile> getFilesForSigning(long userId);

    public void signFile(long fileId, String sign);
}
