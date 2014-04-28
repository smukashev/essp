package kz.bsbnb.usci.cli.app.command;

import kz.bsbnb.usci.eav.repository.IMetaClassRepository;

/**
 * @author alexandr.motov
 */
public interface IMetaCommand extends ICommand {

    public void setMetaClassRepository(IMetaClassRepository metaClassRepository);

}
