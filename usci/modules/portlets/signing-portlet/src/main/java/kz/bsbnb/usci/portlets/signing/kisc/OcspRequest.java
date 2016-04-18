package kz.bsbnb.usci.portlets.signing.kisc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import kz.bsbnb.usci.portlets.signing.Localization;
import kz.bsbnb.usci.portlets.signing.SignatureValidationException;
import kz.gamma.asn1.ASN1InputStream;
import kz.gamma.asn1.ASN1OctetString;
import kz.gamma.asn1.ASN1Sequence;
import kz.gamma.asn1.DERObject;
import kz.gamma.asn1.DERSequence;
import kz.gamma.asn1.ocsp.BasicOCSPResponse;
import kz.gamma.asn1.ocsp.CertStatus;
import kz.gamma.asn1.ocsp.OCSPRequest;
import kz.gamma.asn1.ocsp.OCSPResponse;
import kz.gamma.asn1.ocsp.OCSPResponseStatus;
import kz.gamma.asn1.ocsp.SingleResponse;
import kz.gamma.functions.InstanceFunctions;
import kz.gamma.functions.OCSPFunctions;
import kz.gamma.functions.SignatureFunctions;

class OcspRequest {

    private final OCSPFunctions ocspFunctions = new OCSPFunctions();
    private final InstanceFunctions instanceFunctions = new InstanceFunctions();
    private final SignatureFunctions signatureFunctions = new SignatureFunctions();
    private final String serviceUrl;

    OcspRequest(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void check(X509Certificate cer) throws SignatureValidationException {
        try {
            byte[] cert_der = cer.getEncoded();
            OCSPRequest request = ocspFunctions.createRequest(cert_der, InstanceFunctions.BYTES_DER, "web-portal_NBRK", null);
            byte[] data = request.getEncoded();
            byte[] response = sendRequest(data);
            int responseStatus = ocspFunctions.getOCSPStatus(response);
            if (responseStatus != OCSPResponseStatus.SUCCESSFUL) {
                throw new SignatureValidationException(Localization.OCSP_REQUEST_FAILED, responseStatus);
            }
            int ocspStatus = getOCSPStatus(response);
            if (ocspStatus != 0) {
                throw new SignatureValidationException(Localization.OCSP_STATUS_CHECK_FAILED, ocspStatus);
            }
        } catch (Exception e) {
            //log.log(Level.SEVERE, "OCSP request failed", e);
            throw new SignatureValidationException(Localization.OCSP_REQUEST_FAILED, e.getMessage());
        }
    }

    /**
     *
     * @param request Запрос в ASN
     *
     * @return Ответ службы
     *
     */
    private byte[] sendRequest(byte[] request) throws Exception {
        DataOutputStream printout = null;
        DataInputStream dataInputStream = null;
        try {
            URLConnection conn = new URL(serviceUrl).openConnection();
            conn.setRequestProperty("content-type", "application/pkixcmp");
            conn.setDoOutput(true);
            printout = new DataOutputStream(conn.getOutputStream());
            printout.write(request);
            printout.flush();
            int responseSize = conn.getContentLength();
            dataInputStream = new DataInputStream(conn.getInputStream());
            byte[] response = new byte[responseSize];
            int totalRead = 0;
            while (totalRead < responseSize) {
                int bytesRead = dataInputStream.read(response, totalRead, responseSize - totalRead);
                if (bytesRead < 0) {
                    break;
                }
                totalRead += bytesRead;
            }
            return response;
        } finally {
            try {
                if (printout != null) {
                    printout.close();
                }
            } catch (IOException e) {
                throw e;
            }
            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * Получение статуса проверяемого сертификата из ответа OCSP. Также
     * осуществляется проверка подписи OCSP ответа
     *
     * @param response Подписанный ответ в DER кодировке (подписанная квитанция)
     * @return Статус OCSP ответа
     * @throws Exception
     */
    private Integer getOCSPStatus(byte[] response) throws Exception {
        ASN1InputStream respStream = new ASN1InputStream(response);
        DERObject respObject = respStream.readObject();
        ASN1Sequence respSeq = (ASN1Sequence) respObject;
        OCSPResponse resp = new OCSPResponse(respSeq);
        ASN1OctetString octetString = resp.getResponseBytes().getResponse();
        ASN1InputStream basicOcspResponseStream = new ASN1InputStream(octetString.getOctets());
        DERObject derObject = basicOcspResponseStream.readObject();
        BasicOCSPResponse basicOCSPResponse = BasicOCSPResponse.getInstance(derObject);
        X509Certificate x509cert = instanceFunctions.getX509CertificateInstance(
                basicOCSPResponse.getCerts().getObjectAt(0).getDERObject().getDEREncoded());
        if (!signatureFunctions.verifySign(basicOCSPResponse.getTbsResponseData().getEncoded(),
                basicOCSPResponse.getSignature().getBytes(), x509cert.getPublicKey())) {
            throw new SignatureValidationException(Localization.OCSP_REQUEST_FAILED, "unverified");
        }
        DERSequence responses = (DERSequence) basicOCSPResponse.getTbsResponseData().getResponses();
        SingleResponse singleResponse = SingleResponse.getInstance(responses.getObjectAt(0));
        CertStatus certStatus = singleResponse.getCertStatus();
        return certStatus.getTagNo();
    }
}
