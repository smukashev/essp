package kz.bsbnb.usci.tools

import groovy.sql.Sql

/**
 * Created by emles on 22.09.17
 */

final def propertiesPath = 'properties/oracle.properties'
final Properties props = new Properties()

block:
{
    props.load(Thread.currentThread().contextClassLoader.getResourceAsStream(propertiesPath))
}

def getSqlCore = {

    def (url, user, password, driver) = [props.getProperty('jdbc.core.url'), props.getProperty('jdbc.core.user'), props.getProperty('jdbc.core.password'), props.getProperty('jdbc.core.driver')]

    return Sql.newInstance(url, user, password, driver)

}

def getSqlShowcase = {

    def (url, user, password, driver) = [props.getProperty('jdbc.showcase.url'), props.getProperty('jdbc.showcase.user'), props.getProperty('jdbc.showcase.password'), props.getProperty('jdbc.showcase.driver')]

    return Sql.newInstance(url, user, password, driver)

}

Sql sql

final def DROP_OBJECTS = '''DECLARE
BEGIN
  FOR r1 IN ( SELECT 'DROP ' || object_type || ' ' || object_name || DECODE ( object_type, 'TABLE', ' CASCADE CONSTRAINTS PURGE' ) AS v_sql
                FROM user_objects
               WHERE object_type IN ( 'TABLE', 'VIEW', 'PACKAGE', 'TYPE', 'PROCEDURE', 'FUNCTION', 'TRIGGER', 'SEQUENCE' )
               ORDER BY object_type,
                        object_name ) LOOP
    BEGIN
        EXECUTE IMMEDIATE r1.v_sql;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
  END LOOP;
END;''' as String


sql = getSqlCore()

println "Starting to clean core..."
sql.execute DROP_OBJECTS



