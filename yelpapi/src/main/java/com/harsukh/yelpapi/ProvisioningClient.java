package com.harsukh.yelpapi;

import android.content.Context;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by harsukh on 6/7/17.
 */

public class ProvisioningClient {

    private String endpoint;
    private SSLSocketFactory pinnedSSLSocketFactory;

    public ProvisioningClient(Context context) throws Exception {
       // this.pinnedSSLSocketFactory = getPinnedSSLSocketFactory(context);
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public AuthConstants.DeviceBuilder getProvisioningInfo() throws JSONException, IOException {
        URL companionInfoEndpoint = new URL(endpoint + "/provision/deviceInfo");

        HttpURLConnection connection = (HttpURLConnection) companionInfoEndpoint.openConnection();

        JSONObject response = doRequest(connection);

        List<String> missingParameters = new ArrayList<String>();
        if (!response.has(AuthConstants.PRODUCT_ID)) {
            missingParameters.add(AuthConstants.PRODUCT_ID);
        }

        if (!response.has(AuthConstants.DSN)) {
            missingParameters.add(AuthConstants.DSN);
        }

        if (!response.has(AuthConstants.SESSION_ID)) {
            missingParameters.add(AuthConstants.SESSION_ID);
        }

        if (!response.has(AuthConstants.CODE_CHALLENGE)) {
            missingParameters.add(AuthConstants.CODE_CHALLENGE);
        }

        if (!response.has(AuthConstants.CODE_CHALLENGE_METHOD)) {
            missingParameters.add(AuthConstants.CODE_CHALLENGE_METHOD);
        }

        if (missingParameters.size() != 0) {
            throw new MissingParametersException(missingParameters);
        }

        String productId = response.getString(AuthConstants.PRODUCT_ID);
        String dsn = response.getString(AuthConstants.DSN);
        String sessionId = response.getString(AuthConstants.SESSION_ID);
        String codeChallenge = response.getString(AuthConstants.CODE_CHALLENGE);
        String codeChallengeMethod = response.getString(AuthConstants.CODE_CHALLENGE_METHOD);

        AuthConstants.DeviceBuilder ret = new AuthConstants.DeviceBuilder(productId, dsn, sessionId, codeChallenge, codeChallengeMethod);
        return ret.build();
    }

    public void postCompanionProvisioningInfo(AuthConstants.ClientBuilder companionProvisioningInfo) throws IOException, JSONException {
        String jsonString = companionProvisioningInfo.toJson().toString();

        URL companionInfoEndpoint = new URL(endpoint + "/provision/companionInfo");

        HttpURLConnection connection = (HttpURLConnection) companionInfoEndpoint.openConnection();

        doRequest(connection, jsonString);
    }

    JSONObject doRequest(HttpURLConnection connection) throws IOException, JSONException {
        return doRequest(connection, null);
    }

    JSONObject doRequest(HttpURLConnection connection, String data) throws IOException, JSONException {
        int responseCode = -1;
        InputStream response = null;
        DataOutputStream outputStream = null;

        try {
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(pinnedSSLSocketFactory);
            }

            connection.setRequestProperty("Content-Type", "application/json");
            if (data != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(data.getBytes());
                outputStream.flush();
                outputStream.close();
            } else {
                connection.setRequestMethod("GET");
            }

            responseCode = connection.getResponseCode();
            response = connection.getInputStream();

            if (responseCode != 204) {
                String responseString = IOUtils.toString(response);
                JSONObject jsonObject = new JSONObject(responseString);
                return jsonObject;
            } else {
                return null;
            }
        } catch (IOException e) {
            if (responseCode < 200 || responseCode >= 300) {
                response = connection.getErrorStream();
                if (response != null) {
                    String responseString = IOUtils.toString(response);
                    throw new RuntimeException(responseString);
                }
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(response);
        }
    }

    private SSLSocketFactory getPinnedSSLSocketFactory(Context context) throws Exception {
        InputStream caCertInputStream = null;
        try {
            //caCertInputStream = context.getResources().openRawResource(R.raw.ca);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate caCert = cf.generateCertificate(caCertInputStream);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("myca", caCert);

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } finally {
            IOUtils.closeQuietly(caCertInputStream);
        }
    }

    public static class MissingParametersException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final List<String> missingParameters;

        public MissingParametersException(List<String> missingParameters) {
            super();
            this.missingParameters = missingParameters;
        }

        @Override
        public String getMessage() {
            return "The following parameters were missing or empty strings: "
                    + StringUtils.join(missingParameters.toArray(), ", ");
        }

        public List<String> getMissingParameters() {
            return missingParameters;
        }
    }
}
