package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Shared;

public interface IRemoteSharedBusiness
{
    public Shared findByC_T(String code, String type);

}
