package kz.bsbnb.usci.brms.rulesingleton;

import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.ConsequenceExceptionHandler;
import org.drools.runtime.rule.WorkingMemory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 */
public class RuleExceptionHandler implements ConsequenceExceptionHandler, Externalizable {

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
    {

    }

    public void writeExternal(ObjectOutput out)
            throws IOException
    {

    }

    public void handleException(Activation activation, WorkingMemory workingMemory, Exception exception)
    {
        throw new RuleException(activation, workingMemory, exception);
    }
}
