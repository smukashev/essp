package kz.bsbnb.usci.cli.app.command;

import kz.bsbnb.usci.eav.repository.IMetaClassRepository;

/**
 * Created by Alexandr.Motov on 22.04.14.
 */
public interface IMetaCommand extends ICommand {

    public void setMetaClassRepository(IMetaClassRepository metaClassRepository);

}
