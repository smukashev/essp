package kz.bsbnb.usci.portlets.signing.kisc;

import kz.bsbnb.usci.portlets.signing.Localization;
import kz.bsbnb.usci.portlets.signing.SignatureValidationException;
import kz.bsbnb.usci.portlets.signing.data.FileSignatureRecord;
import kz.gamma.asn1.*;
import kz.gamma.cms.Pkcs7Data;
import kz.gamma.jce.X509Principal;
import kz.gamma.jce.provider.GammaTechProvider;
import kz.gamma.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aidar.Myrzahanov
 */
public class SignatureChecker {

    private static final String ROOT_CA_NAME = "C=KZ,O=KISC,CN=KISC Root CA";
    private final boolean USE_NEW_EXTRACT_BIN = true;
    private final String binNo;
    private final String ocspServiceUrl;

    Logger log = LoggerFactory.getLogger(SignatureChecker.class);

    public SignatureChecker(String binNo, String ocspServiceUrl) {
        this.binNo = binNo;
        this.ocspServiceUrl = ocspServiceUrl;
    }

    public void checkAndUpdate(FileSignatureRecord record, String signature) throws SignatureValidationException {
        if (Security.getProvider(GammaTechProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new GammaTechProvider());
        }
        if (binNo == null) {
            throw new SignatureValidationException(Localization.BIN_NOT_FOUND_FOR_CREDITOR);
        }
        if (signature == null || signature.isEmpty()) {
            throw new SignatureValidationException(Localization.SIGNATURE_IS_EMPTY);
        }

        try {
            Pkcs7Data pkcs = new Pkcs7Data(Base64.decode(signature));
            X509Certificate certificate = pkcs.getCertificateOfSigner();
            checkHash(pkcs, record.getHash());
            checkDates(certificate);
            checkIssuer(certificate);
            checkBin(certificate);
            checkStatus(certificate);

            record.setSignature(signature);
            record.setInformation(certificate.getSubjectX500Principal().getName());
            record.setSigningTime(retrieveSigningTime(pkcs));
        } catch (SignatureValidationException sve) {
            throw sve;
        } catch (Exception e) {
            //log.log(Level.SEVERE, "", e);
            throw new SignatureValidationException(Localization.UNEXPECTED_EXCEPTION_DURING_SIGNATURE_VALIDATION, e.getMessage());
        }
    }

    private void checkHash(Pkcs7Data pkcs, String signedValue) throws SignatureValidationException {
        if (!pkcs.verify()) {
            throw new SignatureValidationException(Localization.SIGNATURE_IS_INCORRECT);
        }
        try {
            String valueFromSignature = new String(pkcs.getData());
            if (!signedValue.equals(valueFromSignature)) {
                throw new SignatureValidationException(Localization.SIGNATURE_IS_INCORRECT);
            }
        } catch (Exception e) {
            throw new SignatureValidationException(Localization.SIGNATURE_IS_INCORRECT);
        }

    }

    private void checkDates(X509Certificate certificate) throws SignatureValidationException {
        Date now = new Date();
        if (now.after(certificate.getNotAfter())) {
            throw new SignatureValidationException(Localization.SIGNATURE_IS_OVERDUE, certificate.getNotAfter());
        }
        if (now.before(certificate.getNotBefore())) {
            throw new SignatureValidationException(Localization.SIGNATURE_IS_NOT_ACTIVE_YET, certificate.getNotBefore());
        }
    }

    private void checkIssuer(X509Certificate certificate) throws SignatureValidationException {
        String issuerName = certificate.getIssuerX500Principal().getName();
        if (!ROOT_CA_NAME.equals(issuerName)) {
            throw new SignatureValidationException(Localization.WRONG_ROOT_CA_NAME, issuerName);
        }
    }

    private void checkBin(X509Certificate certificate) throws SignatureValidationException, IOException {
        String bin = extractBinFromCertificate(certificate);
        if (bin == null) {
            throw new SignatureValidationException(Localization.SIGNATURE_WITHOUT_BIN);
        }
        if (!bin.equals(binNo)) {
            throw new SignatureValidationException(Localization.SIGNATURE_BIN_DOESNT_MATCH, bin, binNo);
        }
    }

    private String extractBinFromCertificate(X509Certificate certificate) throws IOException, SignatureValidationException {
        if (!USE_NEW_EXTRACT_BIN) {
            return extractBinFromCertificate_Old(certificate);
        } else {
            return extractBinFromCertificate_New(certificate);
        }
    }

    private String extractBinFromCertificate_Old(X509Certificate certificate) throws IOException, SignatureValidationException {

        ASN1InputStream extensionStream = null;

        try {

            byte[] extensionBytes = certificate.getExtensionValue("2.5.29.17");
            if (extensionBytes == null) {
                throw new SignatureValidationException(Localization.SUBJECT_ALTERNATIVE_NAME_FIELD_IS_EMPTY);
            }

            extensionStream = new ASN1InputStream(extensionBytes);
            DEROctetString octetString = (DEROctetString) extensionStream.readObject();
            extensionStream.close();
            extensionStream = new ASN1InputStream(octetString.getOctets());
            DERSequence sequence = (DERSequence) extensionStream.readObject();
            extensionStream.close();
            Enumeration subjectAltNames = sequence.getObjects();
            String authority = null;

            while (subjectAltNames.hasMoreElements()) {
                DERTaggedObject nextElement = (DERTaggedObject) subjectAltNames.nextElement();
                X509Principal x509Principal = new X509Principal(nextElement.getObject().getEncoded());
                String data = x509Principal.toString();
                if (authority == null) {
                    authority = data;
                } else {
                    return data.substring(data.lastIndexOf('=') + 1);
                }
            }

        } finally {
            if (extensionStream != null) {
                try {
                    extensionStream.close();
                } catch (IOException ioe) {
                    //log.log(Level.SEVERE, "", ioe);
                }
            }
        }

        return null;

    }

    private String extractBinFromCertificate_New(X509Certificate userCertificate) throws IOException, SignatureValidationException {

        String[] data = null;
        ASN1InputStream extensionStream = null;

        try {

            byte[] extensionBytes = userCertificate.getExtensionValue("2.5.29.17");
            if (extensionBytes == null) {
                throw new SignatureValidationException(Localization.SUBJECT_ALTERNATIVE_NAME_FIELD_IS_EMPTY);
            }

            extensionStream = new ASN1InputStream(extensionBytes);
            DEROctetString octetString = (DEROctetString) extensionStream.readObject();
            extensionStream.close();
            extensionStream = new ASN1InputStream(octetString.getOctets());
            DERSequence sequence = (DERSequence) extensionStream.readObject();
            extensionStream.close();
            Enumeration subjectAltNames = sequence.getObjects();
            int counter = 0;
            String str1 = null;
            String str2 = null;

            while (subjectAltNames.hasMoreElements()) {
                DERTaggedObject nextElement = (DERTaggedObject) subjectAltNames.nextElement();
                X509Principal x509Principal = new X509Principal(nextElement.getObject().getEncoded());
                if (counter == 1) {
                    String extensionData = x509Principal.toString();
                    Map<String, String> hashmap = convertBIN_IIN(extensionData.trim());
                    str1 = hashmap.get("SERIALNUMBER"); // BIN or IIN
                } else if (counter == 2) {
                    String extensionData = x509Principal.toString();
                    Map<String, String> hashmap = convertBIN_IIN(extensionData.trim());
                    str2 = hashmap.get("SERIALNUMBER"); // IIN
                }
                counter++;
            }

            if (str2 != null && str1 != null) { // est' i bin i iin
                data = new String[2];
                data[0] = str2;
                data[1] = str1;
            } else if (str1 != null) { // tol'ko iin
                data = new String[1];
                data[0] = str1;
            }

        } finally {
            if (extensionStream != null) {
                try {
                    extensionStream.close();
                } catch (IOException e) {
                }
            }
        }

        if (data == null) return null;
        if (data.length == 2) return data[1]; // BIN
        if (data.length == 1) return data[0]; // IIN

        return null;

    }

    private Map<String, String> convertBIN_IIN(String extensionData) {

        Map<String, String> hashmap = new HashMap<>();
        String[] aliasStr = extensionData.split(",");

        for (int i = 0; i < aliasStr.length; i++) {
            int index_of_equal = aliasStr[i].lastIndexOf("=");
            String str1 = aliasStr[i].substring(index_of_equal + 1);
            String str2 = aliasStr[i].substring(0, index_of_equal);
            hashmap.put(str2, str1);
        }

        return hashmap;

    }

    private void checkStatus(X509Certificate certificate) throws SignatureValidationException {
        if (ocspServiceUrl == null || " ".equals(ocspServiceUrl)) {
            return;
        }
        OcspRequest request = new OcspRequest(ocspServiceUrl);
        request.check(certificate);
    }

    private Date retrieveSigningTime(Pkcs7Data pkcs) throws Exception {
        byte[] attrValue = pkcs.getAttributeByOid("1.2.840.113549.1.9.5");

        ASN1InputStream extensionStream = null;
        try {
            extensionStream = new ASN1InputStream(attrValue);
            DERUTCTime signingTime = (DERUTCTime) extensionStream.readObject();
            return signingTime.getAdjustedDate();
        } finally {
            if (extensionStream != null) {
                try {
                    extensionStream.close();
                } catch (IOException ioe) {
                    //log.log(Level.SEVERE, "", ioe);
                }
            }
        }
    }

}
