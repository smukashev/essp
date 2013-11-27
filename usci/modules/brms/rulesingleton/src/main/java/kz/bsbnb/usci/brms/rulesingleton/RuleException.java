package kz.bsbnb.usci.brms.rulesingleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.drools.definition.rule.Rule;
import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.WorkingMemory;

/**
 *
 */
public class RuleException extends RuntimeException {

    private final WorkingMemory workingMemory;
    private final Activation activation;

    public RuleException(final Activation activation, final WorkingMemory workingMemory, final Exception exception)
    {
        super(exception);
        this.activation = activation;
        this.workingMemory = workingMemory;
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder( "Exception executing consequence for " );
        if( activation != null && ( activation.getRule() ) != null )
        {
            Rule rule = activation.getRule();
            String ruleName = rule.getName();
            sb.append("rule [\"").append( ruleName ).append( "\"]. " );
        }
        else
        {
            sb.append( "rule, name unknown" );
        }
        Throwable throwable = ExceptionUtils.getRootCause(getCause());
        sb.append("The thrown exception is [").append(throwable).append("]. ");
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return getMessage();
    }

}
