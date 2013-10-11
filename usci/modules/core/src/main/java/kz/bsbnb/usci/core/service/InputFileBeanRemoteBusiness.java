package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;

public interface InputFileBeanRemoteBusiness
{
    public InputFile getInputFileByInputInfo(InputInfo inputInfo);
    public void insertInputFile(InputFile inputFile);
}
