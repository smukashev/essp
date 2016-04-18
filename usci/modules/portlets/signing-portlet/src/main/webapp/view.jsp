<%-- 
    Document   : view
    Created on : Sep 12, 2013, 5:33:16 PM
    Author     : Aidar.Myrzahanov
--%>

<%@page import="kz.bsbnb.usci.portlets.signing.data.DataProvider"%>
<%@page import="kz.bsbnb.usci.portlets.signing.data.BeanDataProvider"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<portlet:defineObjects/>

<link type="text/css" rel="stylesheet" href="${PortalUrl}${ContextPath}/css/style.css" />
<script type="text/javascript">

    function isEmpty(value) {
        return value === undefined || value === null || value.length === 0;
    }

    function getSelectedProfileName() {
        var profilesSelect = document.getElementById("profilesSelect");
        var profile = profilesSelect.options[profilesSelect.selectedIndex];
        if (!profile) {
            return "";
        }
        return profile.text;
    }

    function getProfiles() {
        var profilesString = document.app.getProfileNames('|');
        return profilesString.split('|');
    }

    function getCertificates(profile, password) {
        var certificatesInfo = document.app.getCertificatesInfo(profile, password, 0, '', true, false, '|');
        return certificatesInfo.split('|');
    }

    function signHash(value, certificate, profile, password) {
        return document.app.createPKCS7(value, 0, null, certificate, true, profile, password, '1.3.6.1.4.1.6801.1.5.8', true);
    }

    function loadProfiles() {
        var profilesSelect = document.getElementById("profilesSelect");
        profilesSelect.length = 0;
        var profiles = getProfiles();
        var selectedValue = '';
        for (var i = 0; i < profiles.length; i++) {
            var opt = document.createElement('option');
            opt.value = profiles[i];
            opt.text = profiles[i];
            profilesSelect.add(opt, null);
            if (opt.value === 'profile://FSystem') {
                selectedValue = opt.value;
            }
        }
        document.getElementById('profilePassword').value = '';
        if (!isEmpty(selectedValue)) {
            profilesSelect.value = selectedValue;
        }
    }

    function loadCertificates() {
        var profile = getSelectedProfileName();
        var password = '';
        if (document.getElementById('passwordCheck').checked) {
            password = document.getElementById('profilePassword').value;
        }
        var certificates = getCertificates(profile, password);
        if (!certificates || (certificates.length === 0)) {
            alert('Не удалось получить список сертификатов');
            return;
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

    function signInput(input, certificate, profile, password) {
        var inputName = input.getAttribute('name');
        if (isEmpty(inputName) || inputName.indexOf('hash', 0) !== 0) {
            return;
        }
        var id = inputName.substring(4);
        var check = document.getElementById('check' + id);
        if (!check.checked) {
            return;
        }
        var pkcs7 = signHash(input.value, certificate, profile, password);
        var sign = document.getElementById('sign' + id);
        sign.value = pkcs7;

        var acceptImage = document.createElement('img');
        acceptImage.src = '${PortalUrl}${ContextPath}/accept.png';
        var signSymbolCell = document.getElementById('signSymbol' + id);
        if (signSymbolCell.childNodes.length === 0) {
            signSymbolCell.appendChild(acceptImage);
        }
    }

    function signAllFiles() {
        var profileName = getSelectedProfileName();
        if (isEmpty(profileName)) {
            alert('Выберите профиль');
            return;
        }
        var selectedCertificate = document.getElementById('certificatesSelect').options[document.getElementById('certificatesSelect').selectedIndex];
        if (!selectedCertificate || isEmpty(selectedCertificate.text)) {
            alert('Выберите сертификат');
            return;
        }
        var certificateName = selectedCertificate.text;
        var profilePassword = document.getElementById('profilePassword').value;
        var inputs = document.getElementsByTagName('input');
        for (var i = 0; i < inputs.length; i++) {
            signInput(inputs[i], certificateName, profileName, profilePassword);
        }
    }

    function showHidePasswordDisplay() {
        var passwordDisplay = document.getElementById('passwordDisplay');
        if (document.getElementById('passwordCheck').checked) {
            passwordDisplay.style.display = 'table-row';
        } else {
            passwordDisplay.style.display = 'none';
        }
    }

</script>
<script type="text/javascript" src="${PortalUrl}${ContextPath}/js/deployJava.js"></script>
<script type="text/javascript">
    var contextPath = '${PortalUrl}${ContextPath}';
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
                Отсутвтует сертификат
                <%--<liferay-ui:message key="message-files-no-certificate"/>--%>
            </div>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${hasInfoOnProcessedFiles}">
                    <c:choose>
                        <c:when test="${hasSuccessfullySignedFiles}">
                            <div class="portlet-msg-info">
                                Подписано добавелно в очередь: ${signedFilenames}
                                <%-- <liferay-ui:message key="message-files-signed-and-queued" arguments="${signedFilenames}"/> --%>
                            </div>
                        </c:when>
                    </c:choose>

                    <c:choose>
                        <c:when test="${hasFilesWithErrors}">
                            <div class="portlet-msg-info">
                                Ошибки подписи ${filesWithErrors}
                                <%--<liferay-ui:message key="message-files-signing-errors" arguments="${filesWithErrors}"/>--%>
                            </div>
                        </c:when>
                    </c:choose>

                </c:when>
            </c:choose>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <c:when test="${hasCanceledFiles}">
            <div class="portlet-msg-info">
                <liferay-ui:message key="message-files-canceled" arguments="${canceledFilesCount}"/>
            </div>
        </c:when>
    </c:choose>
    <c:choose>
        <c:when test="${hasAccess}">
            <c:choose>
                <c:when test="${noFilesToSign}">
                    <h2>Нет файлов, ожидающих подписи</h2>
                </c:when>
                <c:otherwise>
                    <form action="<portlet:actionURL><portlet:param name="COMMAND" value="SIGN" /></portlet:actionURL>" method="post">
                        <div class='form-table'>
                            <p>
                                <label for='profilesSelect'>Список профилей: </label>
                                <select id='profilesSelect' name="profiles"></select>
                                <input class='form-element' type="button" value="Загрузить профили" onclick="loadProfiles();"/>
                            </p>
                            <p>
                                <label><input id='passwordCheck' type='checkbox' onclick='showHidePasswordDisplay();' value='false'>Профиль защищен паролем</label>
                            </p>
                            <div id='passwordDisplay' style='display:none'>
                                <label for='profilePassword'>Пароль профиля: </label>
                                <input class='form-element' type="password" value="" id="profilePassword"/>
                            </div>
                            <p>
                                <label for='certificatesSelect'>Сертификат: </label>
                                <select name="certificateInfo" id="certificatesSelect"></select>
                                <input class='form-element' type="button" value="Получить сертификаты из профиля" id="getCertificatesButton" onClick="loadCertificates();"/>
                            </p>
                            <p>
                                <input class='form-element' type="button" id="signButton" value="Подписать выбранные файлы" onclick="signAllFiles();"/>
                            </p>
                        </div>
                        <hr/>


                        <table border="2" width="100%">
                            <tr>
                                <th></th>
                                <th>Имя файла</th>
                                <th>Дата отправки</th>
                                <th>Подписан</th>
                            </tr>
                            <c:forEach var="fileSignatureRecord" items="${inputFiles}">
                                <tr>
                                    <td align='center'><input id="check${fileSignatureRecord.id}" name="check${fileSignatureRecord.id}" type="checkbox" checked="true"/></td>
                                    <td>${fileSignatureRecord.filename}</td>
                                    <td align='center'>
                                            ${fileSignatureRecord.sentDate}
                                        <input id='hash${fileSignatureRecord.id}' type="hidden" name="hash${fileSignatureRecord.id}" value="${fileSignatureRecord.hash}"/>
                                        <input id="sign${fileSignatureRecord.id}" type="hidden" name="sign${fileSignatureRecord.id}"/>
                                    </td>
                                    <td id="signSymbol${fileSignatureRecord.id}"  align='center'></td>
                                </tr>
                            </c:forEach>
                        </table>
                        <input type='submit' name='saveSignatures' value='Сохранить'/>
                        <input type='submit' id='cancelButton' name='cancel' value='Отменить загрузку выбранных файлов'/>
                    </form>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <h2>У вас нет доступа к подписыванию файлов</h2>
        </c:otherwise>
    </c:choose>
</div>