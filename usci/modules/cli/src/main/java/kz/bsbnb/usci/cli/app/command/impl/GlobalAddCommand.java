package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.IGlobalCommand;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

/**
 * Created by maksat on 7/27/15.
 */
public class GlobalAddCommand extends AbstractCommand implements IGlobalCommand {

    public static final String OPTION_TYPE = "type";
    public static final String OPTION_CODE = "code";
    public static final String OPTION_VALUE = "value";
    public static final String OPTION_DESC = "desc";

    private Options options = new Options();

    private IEavGlobalDao eavGlobalDao;

    public GlobalAddCommand() {
        {
            Option option = new Option(OPTION_TYPE, OPTION_TYPE, true, "Type group of global.");
            option.setRequired(true);
            option.setArgs(1);
            option.setOptionalArg(false);
            option.setArgName(OPTION_TYPE);
            option.setType(String.class);
            options.addOption(option);
        }
        {
            Option option = new Option(OPTION_CODE, OPTION_CODE, true, "Code of global.");
            option.setRequired(true);
            option.setArgs(1);
            option.setOptionalArg(false);
            option.setArgName(OPTION_CODE);
            option.setType(String.class);
            options.addOption(option);
        }
        {
            Option option = new Option(OPTION_VALUE, OPTION_VALUE, true, "Value of global.");
            option.setRequired(true);
            option.setArgs(1);
            option.setOptionalArg(false);
            option.setArgName(OPTION_VALUE);
            option.setType(String.class);
            options.addOption(option);
        }
        {
            Option option = new Option(OPTION_DESC, OPTION_DESC, true, "Description of global.");
            option.setRequired(true);
            option.setArgs(1);
            option.setOptionalArg(false);
            option.setArgName(OPTION_DESC);
            option.setType(String.class);
            options.addOption(option);
        }
    }

    @Override
    public void run(String[] args) {
        Object o = null;

        String type = null;
        String code = null;
        String value = null;
        String desc = null;

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.hasOption(OPTION_TYPE)) {
                type = (String) getParsedOption(commandLine, OPTION_TYPE);
            }

            if (commandLine.hasOption(OPTION_CODE)) {
                code = (String) getParsedOption(commandLine, OPTION_CODE);
            }

            if (commandLine.hasOption(OPTION_VALUE)) {
                value = (String) getParsedOption(commandLine, OPTION_VALUE);
            }

            if (commandLine.hasOption(OPTION_DESC)) {
                desc = (String) getParsedOption(commandLine, OPTION_DESC);
            }

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            helpFormatter.printHelp(getCustomUsageString("global add", options), options);
            return;
        }

        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(code)) {
            System.out.println("Тип и код не должны быть пустыми;");
        }

        EavGlobal eavGlobal = eavGlobalDao.get(type, code);

        if (eavGlobal == null) {
            eavGlobal = new EavGlobal();
            eavGlobal.setType(type);
            eavGlobal.setCode(code);
        }

        eavGlobal.setValue(value);
        eavGlobal.setDescription(desc);

        if (eavGlobal.getId() > 0)
            eavGlobalDao.update(eavGlobal);
        else
            eavGlobalDao.insert(eavGlobal);
    }

    public void setEavGlobalDao(IEavGlobalDao eavGlobalDao) {
        this.eavGlobalDao = eavGlobalDao;
    }
}
