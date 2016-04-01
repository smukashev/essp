package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.IMetaCommand;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.commons.cli.*;

/**
 * @author alexandr.motov
 */
public class MetaKeyCommand extends AbstractCommand implements IMetaCommand {
    public static final String OPTION_ID = "i";
    public static final String LONG_OPTION_ID = "id";
    public static final String OPTION_NAME = "n";
    public static final String LONG_OPTION_NAME = "name";
    public static final String OPTION_ATTRIBUTE = "a";
    public static final String LONG_OPTION_ATTRIBUTE = "attribute";
    public static final String OPTION_OPTIONAL = "o";
    public static final String LONG_OPTION_OPTIONAL = "optional";

    public static final Long DEFAULT_ID = null;
    public static final String DEFAULT_NAME = null;
    public static final String DEFAULT_ATTRIBUTE = null;
    public static final boolean DEFAULT_OPTIONAL = false;

    private IMetaClassRepository metaClassRepository;
    private Options options = new Options();

    public MetaKeyCommand() {
        Option idOption = new Option(OPTION_ID, LONG_OPTION_ID, true, "ID to find instance of MetaClass.");
        idOption.setRequired(false);
        idOption.setArgs(1);
        idOption.setOptionalArg(false);
        idOption.setType(Long.class);
        options.addOption(idOption);

        Option nameOption = new Option(OPTION_NAME, LONG_OPTION_NAME, true, "Name to find instance of MetaClass.");
        nameOption.setRequired(false);
        nameOption.setArgs(1);
        nameOption.setOptionalArg(false);
        nameOption.setType(String.class);
        options.addOption(nameOption);

        Option attributeOption = new Option(OPTION_ATTRIBUTE, LONG_OPTION_ATTRIBUTE, true, "Attribute name to set the key.");
        attributeOption.setRequired(true);
        attributeOption.setArgs(1);
        attributeOption.setOptionalArg(false);
        attributeOption.setType(String.class);
        options.addOption(attributeOption);

        Option optionalOption = new Option(OPTION_OPTIONAL, LONG_OPTION_OPTIONAL, false, "Attribute name to set the optional key.");
        optionalOption.setRequired(false);
        optionalOption.setArgs(0);
        optionalOption.setOptionalArg(true);
        optionalOption.setType(String.class);
        options.addOption(optionalOption);
    }

    @Override
    public void run(String args[]) {
        Object o;
        Long id = DEFAULT_ID;
        String name = DEFAULT_NAME;
        String attribute = DEFAULT_ATTRIBUTE;
        boolean isOptional = false;

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.hasOption(OPTION_ID)) {
                o = getParsedOption(commandLine, OPTION_ID);
                if (o != null) {
                    id = (Long) o;
                }
            }

            if (commandLine.hasOption(OPTION_NAME)) {
                o = getParsedOption(commandLine, OPTION_NAME);
                if (o != null) {
                    name = (String) o;
                }
            }

            if (commandLine.hasOption(OPTION_ATTRIBUTE)) {
                o = getParsedOption(commandLine, OPTION_ATTRIBUTE);
                if (o != null) {
                    attribute = (String) o;
                }
            }

            if (commandLine.hasOption(OPTION_OPTIONAL)) {
                isOptional = true;
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            helpFormatter.printHelp(getCustomUsageString("meta key", options), options);

            return;
        }

        if (metaClassRepository == null) {
            throw new RuntimeException(Errors.compose(Errors.E221));
        }

        MetaClass meta = null;
        if (id != null && id != 0) {
            meta = metaClassRepository.getMetaClass(id);
        } else {
            if (name != null) {
                meta = metaClassRepository.getMetaClass(name);
            }
        }

        if (meta == null) {
            System.out.println("No such instance of MetaClass.");
        } else {
            IMetaAttribute attr = meta.getMetaAttribute(attribute);

            if (attr != null) {
                if (isOptional) {
                    attr.setOptionalKey(true);
                }
                attr.setKey(!attr.isKey());
                metaClassRepository.saveMetaClass(meta);
            } else {
                System.out.println("No such instance of MetaAttribute with name: " + attribute);
            }
        }
    }

    @Override
    public void setMetaClassRepository(IMetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }
}
