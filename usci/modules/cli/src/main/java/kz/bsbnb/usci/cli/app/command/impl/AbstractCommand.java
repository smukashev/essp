package kz.bsbnb.usci.cli.app.command.impl;

import kz.bsbnb.usci.cli.app.command.ICommand;
import org.apache.commons.cli.*;

import java.util.List;

/**
 * Created by Alexandr.Motov on 22.04.14.
 */
public abstract class AbstractCommand implements ICommand {

    protected CommandLineParser commandLineParser = new PosixParser();
    protected HelpFormatter helpFormatter = new HelpFormatter();

    public Object getParsedOption(CommandLine commandLine, String option) throws ParseException {
        if (commandLine.hasOption(option)) {
            Object o = commandLine.getParsedOptionValue(option);
            /*if (o != null)
            {
                System.out.println("Option " + option + " parsed as " + o + " which is instance of " + o.getClass().getCanonicalName());
            }*/
            return o;
        }
        return null;
    }

    public String getCustomOptionString(Option option) {
        StringBuilder stringBuilder = new StringBuilder();

        if (!option.isRequired()) {
            stringBuilder.append("[");
        }

        stringBuilder.append("-");
        stringBuilder.append(option.getOpt());

        for (int i = 1; i <= option.getArgs(); i++) {
            stringBuilder.append(" ");
            stringBuilder.append("<");
            stringBuilder.append(option.getArgName());
            stringBuilder.append(">");
        }

        if (!option.isRequired()) {
            stringBuilder.append("]");
        }

        return stringBuilder.toString();
    }

    public String getCustomUsageString(String command, Options options) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(command);

        List requiredOpts = options.getRequiredOptions();
        for (Object requiredOpt: requiredOpts) {
            Option option = options.getOption((String)requiredOpt);
            stringBuilder.append(" ");
            stringBuilder.append(getCustomOptionString(option));
        }

        for (Object option: options.getOptions()) {
            String opt = ((Option)option).getOpt();
            if (requiredOpts.contains(opt))
            {
                continue;
            }

            stringBuilder.append(" ");
            stringBuilder.append(getCustomOptionString((Option) option));
        }

        return stringBuilder.toString();
    }

}
