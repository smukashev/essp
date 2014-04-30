package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.IMetaCommand;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.commons.cli.*;

/**
 * @author alexandr.motov
 */
public class MetaCreateCommand extends AbstractCommand implements IMetaCommand {

    public enum AttributeKeyType
    {
        ALL(ComplexKeyTypes.ALL), ANY(ComplexKeyTypes.ANY);

        private ComplexKeyTypes complexKeyType;

        AttributeKeyType(ComplexKeyTypes complexKeyType)
        {
            this.complexKeyType = complexKeyType;
        }

        public ComplexKeyTypes getComplexKeyType() {
            return complexKeyType;
        }
    }

    public static final String OPTION_NAME = "n";
    public static final String LONG_OPTION_NAME = "name";
    public static final String OPTION_REFERENCE = "r";
    public static final String LONG_OPTION_REFERENCE = "reference";
    public static final String OPTION_KEY_TYPE = "kt";
    public static final String LONG_OPTION_KEY_TYPE = "keytype";

    public static final String DEFAULT_NAME = null;
    public static final boolean DEFAULT_REFERENCE = false;
    public static final AttributeKeyType DEFAULT_KEY_TYPE = AttributeKeyType.ALL;

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

        Option keyTypeOption = new Option(OPTION_KEY_TYPE, LONG_OPTION_KEY_TYPE, true,
                "Complex key type for the new instance of IMetaSet.");
        keyTypeOption.setRequired(false);
        keyTypeOption.setArgs(1);
        keyTypeOption.setOptionalArg(false);
        keyTypeOption.setArgName(LONG_OPTION_KEY_TYPE);
        keyTypeOption.setType(String.class);
        options.addOption(keyTypeOption);
    }

    @Override
    public void run(String args[]) {
        String name = DEFAULT_NAME;
        boolean isReference = DEFAULT_REFERENCE;
        AttributeKeyType keyType = DEFAULT_KEY_TYPE;

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
            Object o;

            if(commandLine.hasOption(OPTION_NAME)) {
                o = getParsedOption(commandLine, OPTION_NAME);
                if (o != null) {
                    name = (String) o;
                }
            }

            if(commandLine.hasOption(OPTION_REFERENCE)) {
                isReference = true;
            }

            if(commandLine.hasOption(OPTION_KEY_TYPE)) {
                o = getParsedOption(commandLine, OPTION_KEY_TYPE);
                if (o != null) {
                    keyType = AttributeKeyType.valueOf(((String)o).toUpperCase());
                }
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
        meta.setComplexKeyType(keyType.getComplexKeyType());

        metaClassRepository.saveMetaClass(meta);
    }

    @Override
    public void setMetaClassRepository(IMetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }
}
