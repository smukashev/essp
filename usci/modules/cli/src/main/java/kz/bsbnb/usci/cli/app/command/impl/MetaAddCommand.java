package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.IMetaCommand;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author alexandr.motov
 */
public class MetaAddCommand extends AbstractCommand implements IMetaCommand {

    public enum AttributeType {
        DOUBLE(DataTypes.DOUBLE),
        INTEGER(DataTypes.INTEGER),
        STRING(DataTypes.STRING),
        BOOLEAN(DataTypes.BOOLEAN),
        DATE(DataTypes.DATE),
        META_CLASS;

        private DataTypes dataType;

        AttributeType() {}

        AttributeType(DataTypes dataType) {
            this.dataType = dataType;
        }

        public DataTypes getDataType() {
            return dataType;
        }
    }

    public enum AttributeKeyType {
        ALL(ComplexKeyTypes.ALL), ANY(ComplexKeyTypes.ANY);

        private ComplexKeyTypes complexKeyType;

        AttributeKeyType(ComplexKeyTypes complexKeyType) {
            this.complexKeyType = complexKeyType;
        }

        public ComplexKeyTypes getComplexKeyType() {
            return complexKeyType;
        }
    }

    public static final String OPTION_NAME = "n";
    public static final String LONG_OPTION_NAME = "name";

    public static final String OPTION_ATTRIBUTE = "a";
    public static final String LONG_OPTION_ATTRIBUTE = "attribute";

    public static final String OPTION_TYPE = "t";
    public static final String LONG_OPTION_TYPE = "type";

    public static final String OPTION_KEY_TYPE = "kt";
    public static final String LONG_OPTION_KEY_TYPE = "keytype";

    public static final String OPTION_ARRAY = "ar";
    public static final String LONG_OPTION_ARRAY = "array";

    public static final String OPTION_CUMULATIVE = "cm";
    public static final String LONG_OPTION_CUMULATIVE = "cumulative";

    public static final String OPTION_CHILD_NAME = "cn";
    public static final String LONG_OPTION_CHILD_NAME = "childname";

    public static final String OPTION_IMMUTABLE = "im";
    public static final String LONG_OPTION_IMMUTABLE = "immutable";

    public static final String OPTION_FINAL = "f";
    public static final String LONG_OPTION_FINAL = "final";

    public static final String OPTION_REQUIRED = "r";
    public static final String LONG_OPTION_REQUIRED = "required";

    public static final String DEFAULT_NAME = null;
    public static final String DEFAULT_ATTRIBUTE = null;
    public static final AttributeType DEFAULT_TYPE = null;
    public static final AttributeKeyType DEFAULT_KEY_TYPE = AttributeKeyType.ALL;
    public static final String DEFAULT_CHILD_NAME = null;
    public static final boolean DEFAULT_ARRAY = false;
    public static final boolean DEFAULT_IMMUTABLE = false;
    public static final boolean DEFAULT_CUMULATIVE = false;
    public static final boolean DEFAULT_FINAL = false;
    public static final boolean DEFAULT_REQUIRED = false;

    private IMetaClassRepository metaClassRepository;
    private Options options = new Options();

    public MetaAddCommand() {
        Option nameOption = new Option(OPTION_NAME, LONG_OPTION_NAME, true,
                "Class name to find instance of MetaClass.");
        nameOption.setRequired(true);
        nameOption.setArgs(1);
        nameOption.setOptionalArg(false);
        nameOption.setArgName(LONG_OPTION_NAME);
        nameOption.setType(String.class);
        options.addOption(nameOption);

        Option attributeOption = new Option(OPTION_ATTRIBUTE, LONG_OPTION_ATTRIBUTE, true,
                "Name for the new instance of IMetaAttribute.");
        attributeOption.setRequired(true);
        attributeOption.setArgs(1);
        attributeOption.setOptionalArg(false);
        attributeOption.setArgName(LONG_OPTION_ATTRIBUTE);
        attributeOption.setType(String.class);
        options.addOption(attributeOption);

        Option typeOption = new Option(OPTION_TYPE, LONG_OPTION_TYPE, true,
                "Type for the new instance of IMetaAttribute.");
        typeOption.setRequired(true);
        typeOption.setArgs(1);
        typeOption.setOptionalArg(false);
        typeOption.setArgName(LONG_OPTION_TYPE);
        typeOption.setType(String.class);
        options.addOption(typeOption);

        Option keyTypeOption = new Option(OPTION_KEY_TYPE, LONG_OPTION_KEY_TYPE, true,
                "Complex key type for the new instance of IMetaSet.");
        keyTypeOption.setRequired(false);
        keyTypeOption.setArgs(1);
        keyTypeOption.setOptionalArg(false);
        keyTypeOption.setArgName(LONG_OPTION_KEY_TYPE);
        keyTypeOption.setType(String.class);
        options.addOption(keyTypeOption);

        Option childNameOption = new Option(OPTION_CHILD_NAME, LONG_OPTION_CHILD_NAME, true,
                "Class name to find child instance of MetaClass.");
        childNameOption.setRequired(false);
        childNameOption.setArgs(1);
        childNameOption.setOptionalArg(false);
        childNameOption.setArgName(LONG_OPTION_CHILD_NAME);
        childNameOption.setType(String.class);
        options.addOption(childNameOption);

        Option arrayOption = new Option(OPTION_ARRAY, LONG_OPTION_ARRAY, false,
                "Array flag for new instance of MetaAttribute.");
        arrayOption.setArgs(0);
        arrayOption.setRequired(false);
        options.addOption(arrayOption);

        Option cumulativeOption = new Option(OPTION_CUMULATIVE, LONG_OPTION_CUMULATIVE, false,
                "Cumulative flag for new instance of MetaAttribute.");
        arrayOption.setArgs(0);
        arrayOption.setRequired(false);
        options.addOption(cumulativeOption);

        Option immutableOption = new Option(OPTION_IMMUTABLE, LONG_OPTION_IMMUTABLE, false,
                "Immutable flag for new instance of MetaAttribute.");
        immutableOption.setArgs(0);
        immutableOption.setRequired(false);
        options.addOption(immutableOption);

        Option finalOption = new Option(OPTION_FINAL, LONG_OPTION_FINAL, false,
                "Final flag for new instance of MetaAttribute.");
        finalOption.setArgs(0);
        finalOption.setRequired(false);
        options.addOption(finalOption);

        Option requiredOption = new Option(OPTION_REQUIRED, LONG_OPTION_REQUIRED, false,
                "Required flag for new instance of MetaAttribute.");
        requiredOption.setArgs(0);
        requiredOption.setRequired(false);
        options.addOption(requiredOption);
    }

    @Override
    public void run(String args[]) {
        Object o = null;

        String name = DEFAULT_NAME;
        String attribute = DEFAULT_ATTRIBUTE;
        String childName = DEFAULT_CHILD_NAME;
        AttributeType type = DEFAULT_TYPE;
        AttributeKeyType keyType = DEFAULT_KEY_TYPE;
        boolean isArray = DEFAULT_ARRAY;
        boolean isImmutable = DEFAULT_IMMUTABLE;
        boolean isFinal = DEFAULT_FINAL;
        boolean isRequired = DEFAULT_REQUIRED;
        boolean isCumulative = DEFAULT_CUMULATIVE;

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);

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

            if (commandLine.hasOption(OPTION_TYPE)) {
                o = getParsedOption(commandLine, OPTION_TYPE);
                if (o != null) {
                    type = AttributeType.valueOf(((String) o).toUpperCase());
                }
            }

            if (commandLine.hasOption(OPTION_KEY_TYPE)) {
                o = getParsedOption(commandLine, OPTION_KEY_TYPE);
                if (o != null) {
                    keyType = AttributeKeyType.valueOf(((String) o).toUpperCase());
                }
            }

            if (commandLine.hasOption(OPTION_CHILD_NAME)) {
                o = getParsedOption(commandLine, OPTION_CHILD_NAME);
                if (o != null) {
                    childName = (String) o;
                }
            }

            if (commandLine.hasOption(OPTION_ARRAY)) {
                isArray = true;
            }

            if (commandLine.hasOption(OPTION_CUMULATIVE)) {
                isCumulative = true;
            }

            if (commandLine.hasOption(OPTION_IMMUTABLE)) {
                isImmutable = true;
            }

            if (commandLine.hasOption(OPTION_FINAL)) {
                isFinal = true;
            }

            if (commandLine.hasOption(OPTION_REQUIRED)) {
                isRequired = true;
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            helpFormatter.printHelp(getCustomUsageString("meta add", options), options);

            return;
        }

        if (metaClassRepository == null) {
            throw new RuntimeException("Instance of IMetaClassRepository can not be null.");
        }

        MetaClass metaClass = null;
        if (name != null) {
            metaClass = metaClassRepository.getMetaClass(name);
        } else {
            System.out.println("Имя не должно быть NULL;");
            return;
        }

        if (type == AttributeType.META_CLASS) {
            MetaClass childMetaClass = metaClassRepository.getMetaClass(childName);

            if (isArray) {
                MetaSet setToAdd = new MetaSet(childMetaClass);
                if (keyType != null) {
                    setToAdd.setArrayKeyType(keyType.getComplexKeyType());
                }
                MetaAttribute metaAttribute = new MetaAttribute(false, false, setToAdd);
                metaAttribute.setImmutable(isImmutable);
                metaAttribute.setFinal(isFinal);
                metaAttribute.setRequired(isRequired);
                metaAttribute.setCumulative(isCumulative);
                metaClass.setMetaAttribute(attribute, metaAttribute);
            } else {
                MetaAttribute metaAttribute = new MetaAttribute(false, false, childMetaClass);
                metaAttribute.setImmutable(isImmutable);
                metaAttribute.setFinal(isFinal);
                metaAttribute.setRequired(isRequired);
                metaAttribute.setCumulative(isCumulative);
                metaClass.setMetaAttribute(attribute, metaAttribute);
            }
        } else {
            MetaValue metaValue = new MetaValue(type.getDataType());

            if (isArray) {
                MetaSet setToAdd = new MetaSet(metaValue);
                if (keyType != null) {
                    setToAdd.setArrayKeyType(keyType.getComplexKeyType());
                }
                MetaAttribute metaAttribute = new MetaAttribute(false, false, setToAdd);
                metaAttribute.setImmutable(isImmutable);
                metaAttribute.setFinal(isFinal);
                metaAttribute.setRequired(isRequired);
                metaAttribute.setCumulative(isCumulative);
                metaClass.setMetaAttribute(attribute, metaAttribute);
            } else {
                MetaAttribute metaAttribute = new MetaAttribute(false, false, metaValue);
                metaAttribute.setImmutable(isImmutable);
                metaAttribute.setFinal(isFinal);
                metaAttribute.setRequired(isRequired);
                metaAttribute.setCumulative(isCumulative);

                metaClass.setMetaAttribute(attribute, metaAttribute);
            }
        }

        metaClassRepository.saveMetaClass(metaClass);
    }

    @Override
    public void setMetaClassRepository(IMetaClassRepository metaClassRepository) {
        this.metaClassRepository = metaClassRepository;
    }
}
