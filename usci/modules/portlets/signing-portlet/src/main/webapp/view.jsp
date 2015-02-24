<%-- 
    Document   : index
    Created on : Sep 12, 2013, 5:33:16 PM
    Author     : Aidar.Myrzahanov
--%>

<%@page import="com.bsbnb.creditregistry.portlets.signing.data.DataProvider"%>
<%@page import="com.bsbnb.creditregistry.portlets.signing.data.BeanDataProvider"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<portlet:defineObjects />

<link type="text/css" rel="stylesheet" src="/signing-portlet/css/style.css" />
<script type="text/javascript">

    function loadProfiles() {
        var profilesString = document.app.getProfileNames('|');
        var profilesSelect = document.getElementById("profilesSelect");
        profilesSelect.length = 0;
        var profiles = profilesString.split('|');

        for (var i = 0; i < profiles.length; i++) {
            var opt = document.createElement('option');
            opt.value = profiles[i];
            opt.text = profiles[i];
            profilesSelect.add(opt, null);
        }
    }

    function performAppletCode(count) {
        var applet = document.app;

        if (!applet.getProfileNames && count > 0) {
            setTimeout(function() {
                performAppletCode(--count);
            }, 2000);
        }
        else if (applet.getProfileNames) {
            loadProfiles();
        }
        else {
            alert('applet failed to load');
        }
    }

    function loadCertificates() {
        var profile = document.getElementById('profilesSelect').options[document.getElementById('profilesSelect').selectedIndex].text;
        var password = document.getElementById('profilePassword').value;
        var sCertificates = document.app.getCertificatesInfo(profile, password, 0, '', true, false, '|');
        var certificates = [];
        if (sCertificates) {
            certificates = sCertificates.split('|');
        }
        var certificatesSelect = document.getElementById('certificatesSelect');
        certificatesSelect.length = 0;
        for (var i = 0; i < certificates.length; i++) {
            var opt = document.createElement('option');
            opt.value = certificates[i];
            opt.text = certificates[i];
            certificatesSelect.add(opt, null);
        }
    }

    function signAllFiles() {
        var inputs = document.getElementsByTagName('input');
        for (var i = 0; i < inputs.length; i++) {
            var inputName = inputs[i].getAttribute('name');
            if (inputName != null && inputName.indexOf("hash") == 0) {
                var valueToSign = inputs[i].value;
                var profileName = document.getElementById('profilesSelect').options[document.getElementById('profilesSelect').selectedIndex].text;
                var certificateName = document.getElementById('certificatesSelect').options[document.getElementById('certificatesSelect').selectedIndex].text;
                var profilePassword = document.getElementById('profilePassword').value;
                var algorithmId = '1.3.6.1.4.1.6801.1.5.8';
                var pkcs7 = document.app.createPKCS7(valueToSign, 0, null, certificateName, true, profileName, profilePassword, algorithmId, true);
                var id = inputName.substring(4);
                var sign = document.getElementById("sign" + id);
                sign.value = pkcs7;

                var acceptImage = document.createElement('img');
                acceptImage.src = '/signing-portlet/accept.png';
                var signSymbolCell = document.getElementById("signSymbol" + id);
                signSymbolCell.appendChild(acceptImage);
            }
        }
    }

</script>
<script type="text/javascript" src="/signing-portlet/js/deployJava.js"></script>
<script type="text/javascript">
        var contextPath = '/signing-portlet';
    var attributes = {
        codebase: './',
        code: 'kz.gamma.TumarCSP.class',
        archive: contextPath + '/lib/commons-logging.jar,' + contextPath + '/lib/xmlsec-1.3.0.jar,' + contextPath + '/lib/crypto.gammaprov.jar,' + contextPath + '/lib/crypto-common.jar,' + contextPath + '/lib/sign-applet.jar',
        width: 0,
        height: 0,
        vspace: 0,
        hspace: 0,
        name: 'app'
    };
    var parameters = {java_arguments: '-Xmx256m'};
    var version = '1.6';
    deployJava.runApplet(attributes, parameters, version);
</script>


<div id="hello">
    <c:choose>
        <c:when test="${noCertificate}">
            <div class="portlet-msg-error">
                <liferay-ui:message key="message-files-no-certificate"/>
            </div>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${hasInfoOnProcessedFiles}">
                    <c:choose>
                        <c:when test="${certificateSuccess}">
                            <div class="portlet-msg-info">
                                <liferay-ui:message key="message-files-certificate-success" arguments="${certificate}"/>
                            </div>
                            <div class="portlet-msg-info">
                                <liferay-ui:message key="message-files-signed-and-queued" arguments="${processedFilenames}"/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="portlet-msg-error">
                                <liferay-ui:message key="message-files-certificate-fail" arguments="${certificate}"/>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
            </c:choose>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${hasAccess}">
            <c:choose>
                <c:when test="${noFilesToSign}">
                    <h2>Нет файлов, ожидающих подписи</h2>
                </c:when>
                <c:otherwise>
                    <form action="<portlet:actionURL><portlet:param name="COMMAND" value="SIGN"/>
                          </portlet:actionURL>" method="post">
                        <b>Список профилей: </b><select id="profilesSelect" name="profiles"></select>

                        <input type="button" value="Загрузить профили" onclick="loadProfiles();"/>
                        <br/>
                        <b>Пароль профиля: </b><input type="password" value="" id="profilePassword"/>
                        <br/>
                        <input type="button" value="Получить сертификаты из профайла" id="getCertificatesButton" onClick="loadCertificates();"/>
                        <br/>
                        <select name="certificateInfo" id="certificatesSelect"></select>
                        <br/>
                        <input type="button" id="signButton" value="Подписать все файлы" onclick="signAllFiles();"/>

                        <hr/>


                        <table border="2" width="100%">
                            <tr>
                                <th>Имя файла</th>
                                <th>Дата отправки</th>
                                <th>Подписан</th>
                            </tr>
                            <c:forEach var="fileSignatureRecord" items="${inputFiles}">
                                <tr>
                                    <td>${fileSignatureRecord.filename}</td>
                                    <td>
                                        ${fileSignatureRecord.sentDate}
                                        <input id="hash${fileSignatureRecord.id}" type="hidden" name="hash${fileSignatureRecord.id}" value="${fileSignatureRecord.hash}"/>
                                        <input id="sign${fileSignatureRecord.id}" type="hidden" name="sign${fileSignatureRecord.id}"/>
                                    </td>
                                    <td id="signSymbol${fileSignatureRecord.id}"></td>
                                </tr>
                            </c:forEach>
                        </table>
                        <input type="submit" value="Сохранить"/>
                    </form>
                    <hr/>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <h2>У вас нет доступа к подписыванию файлов</h2>
        </c:otherwise>
    </c:choose>
</div>