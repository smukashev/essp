package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.ICommand;
import kz.bsbnb.usci.cli.app.command.IMetaCommand;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.commons.cli.*;

/**
 * Created by Alexandr.Motov on 22.04.14.
 */
public class MetaCreateCommand extends AbstractCommand implements IMetaCommand {

    public static final String OPTION_NAME = "n";
    public static final String LONG_OPTION_NAME = "name";
    public static final String OPTION_REFERENCE = "r";
    public static final String LONG_OPTION_REFERENCE = "reference";

    public static final String DEFAULT_NAME = null;
    public static final boolean REFERENCE = false;

    private IMetaClassRepository metaClassRepository;
    private Options options = new Options();

    public MetaCreateCommand()
    {
        Option nameOption = new Option(OPTION_NAME, LONG_OPTION_NAME, true,
                "Name for new instance of MetaClass.");
        nameOption.setRequired(true);
        nameOption.setArgs(1);
        nameOption.setOptionalArg(false);
        nameOption.setType(String.class);
        options.addOption(nameOption);

        Option referenceOption = new Option(OPTION_REFERENCE, LONG_OPTION_REFERENCE, false,
                "Reference flag for new instance of MetaClass.");
        referenceOption.setArgs(0);
        referenceOption.setRequired(false);
        options.addOption(referenceOption);
    }

    @Override
    public void run(String args[]) {
        String name = DEFAULT_NAME;
        boolean isReference = REFERENCE;

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);

            if(commandLine.hasOption(OPTION_NAME)) {
                Object o = getParsedOption(commandLine, OPTION_NAME);
                if (o != null) {
                    name = (String) o;
                }
            }

            if(commandLine.hasOption(OPTION_REFERENCE)) {
                isReference = true;
            }
        }
        catch(ParseException e) {
            System.err.println(e.getMessage());
            helpFormatter.printHelp(getCustomUsageString("meta create", options), options);

            return;
        }

        if (metaClassRepository == null)
        {
            throw new RuntimeException("Instance of IMetaClassRepository can not be null.");
        }

        MetaClass meta = new MetaClass(name);
        meta.setReference(isReference);

        metaClassRepository.saveMetaClass(meta);
    }

    @Override
    public void setMetaClassRepository(IMetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }
}
