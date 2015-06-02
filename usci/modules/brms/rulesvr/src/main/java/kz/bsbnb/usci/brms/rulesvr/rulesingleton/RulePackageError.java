package kz.bsbnb.usci.brms.rulesvr.rulesingleton;

public class RulePackageError
{
    private String packageName;
    private String errorMsg;

    public RulePackageError(String packageName, String errorMsg)
    {
        this.packageName = packageName;
        this.errorMsg = errorMsg;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }
}
