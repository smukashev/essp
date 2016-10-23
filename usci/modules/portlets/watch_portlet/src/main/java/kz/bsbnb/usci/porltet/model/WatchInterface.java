package kz.bsbnb.usci.porltet.model;
/*
 1) this will be deployed application as war
 2) it must be perisistant

 every watch should have :
    1) name
    2) fatal error recepients
    3) acess to usci services
    4) quota
    5) body
*/

public interface WatchInterface {
    public Message singleWatch(USCIServices services, WatchSetting settings) throws Exception;
}