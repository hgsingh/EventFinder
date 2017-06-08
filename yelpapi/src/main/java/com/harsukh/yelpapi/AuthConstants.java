package com.harsukh.yelpapi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harsukh on 6/7/17.
 */

public class AuthConstants {
    public static final String SESSION_ID = "sessionId";
    public static final String CLIENT_ID = "amzn1.application-oa2-client.080d23dcc7374ffcafdd926319249e47";
    public static final String REDIRECT_URI = "redirectUri";
    public static final String AUTH_CODE = "authCode";

    public static final String CODE_CHALLENGE = "codeChallenge";
    public static final String CODE_CHALLENGE_METHOD = "codeChallengeMethod";
    public static final String DSN = "dsn";
    public static final String PRODUCT_ID = "productId";

    public static final class ClientBuilder {
        private String sessionId;
        private String clientId;
        private String redirectUri;
        private String authCode;

        public ClientBuilder() {
        }

        public ClientBuilder(String sessionId, String clientId, String redirectUri, String authCode) {
            this.sessionId = sessionId;
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.authCode = authCode;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getAuthCode() {
            return authCode;
        }

        public ClientBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public ClientBuilder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ClientBuilder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public ClientBuilder setAuthCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public ClientBuilder build() {
            return this;
        }

        public JSONObject toJson() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(AuthConstants.AUTH_CODE, authCode);
                jsonObject.put(AuthConstants.CLIENT_ID, clientId);
                jsonObject.put(AuthConstants.REDIRECT_URI, redirectUri);
                jsonObject.put(AuthConstants.SESSION_ID, sessionId);
                return jsonObject;
            } catch (JSONException e) {
                return null;
            }

        }
    }

    public static final class DeviceBuilder {
        public String codeChallenge;
        public String codeChallengeMethod;
        public String dsn;
        public String productId;
        private String sessionId;

        public DeviceBuilder(String codeChallenge, String codeChallengeMethod, String dsn,
                             String productId, String sessionId) {
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.dsn = dsn;
            this.productId = productId;
            this.sessionId = sessionId;
        }

        public String getCodeChallenge() {
            return codeChallenge;
        }

        public DeviceBuilder setCodeChallenge(String codeChallenge) {
            this.codeChallenge = codeChallenge;
            return this;
        }

        public String getCodeChallengeMethod() {
            return codeChallengeMethod;
        }

        public DeviceBuilder setCodeChallengeMethod(String codeChallengeMethod) {
            this.codeChallengeMethod = codeChallengeMethod;
            return this;
        }

        public String getDsn() {
            return dsn;
        }

        public DeviceBuilder setDsn(String dsn) {
            this.dsn = dsn;
            return this;
        }

        public String getProductId() {
            return productId;
        }

        public DeviceBuilder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public String getSessionId() {
            return sessionId;
        }

        public DeviceBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public DeviceBuilder build() {
            return this;
        }

    }
}
