package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import org.springframework.stereotype.Service;

@Service
public class InputFileBeanRemoteBusinessImpl implements InputFileBeanRemoteBusiness
{
    @Override
    public InputFile getInputFileByInputInfo(InputInfo inputInfo)
    {
        InputFile inputFile = new InputFile();

        inputFile.setFilePath("asd/asd");
        inputFile.setId(1L);

        return null;
    }
}
