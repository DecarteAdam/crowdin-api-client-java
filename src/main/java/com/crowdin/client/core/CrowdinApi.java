package com.crowdin.client.core;

import com.crowdin.client.core.http.HttpClient;
import com.crowdin.client.core.http.JsonTransformer;
import com.crowdin.client.core.http.impl.http.ApacheHttpClient;
import com.crowdin.client.core.http.impl.json.JacksonJsonTransformer;
import com.crowdin.client.core.model.ClientConfig;
import com.crowdin.client.core.model.Credentials;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CrowdinApi {

    protected final HttpClient httpClient;
    protected final ClientConfig clientConfig;
    protected final String url;

    public CrowdinApi(Credentials credentials) {
        this(credentials, ClientConfig.builder()
                .httpClient(new ApacheHttpClient(credentials, new JacksonJsonTransformer(), Collections.emptyMap(), HttpClientBuilder
                        .create()
                        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                        .build()))
                .build());
    }

    public CrowdinApi(Credentials credentials, ClientConfig clientConfig) {
        Map<String, String> defaultHeaders = new HashMap<>();
        if (clientConfig.getUserAgent() != null) {
            defaultHeaders.put("User-Agent", clientConfig.getUserAgent());
        }
        if (clientConfig.getIntegrationUserAgent() != null) {
            defaultHeaders.put("X-Crowdin-Integrations-User-Agent", clientConfig.getIntegrationUserAgent());
        }
        JsonTransformer jsonTransformer = (clientConfig.getJsonTransformer() != null)
            ? clientConfig.getJsonTransformer() : new JacksonJsonTransformer();
        this.httpClient = (clientConfig.getHttpClient() != null)
            ? clientConfig.getHttpClient()
            : new ApacheHttpClient(credentials, jsonTransformer, defaultHeaders, clientConfig.getProxy(), clientConfig.getProxyCreds(), clientConfig.getHttpTimeoutMs());
        this.clientConfig = clientConfig;
        if (credentials.getBaseUrl() != null) {
            if (credentials.getBaseUrl().endsWith("/")) {
                this.url = credentials.getBaseUrl() + "api/v2";
            } else {
                this.url = credentials.getBaseUrl() + "/api/v2";
            }
        } else {
            if (credentials.getOrganization() != null) {
                this.url = "https://" + credentials.getOrganization() + ".api.crowdin.com/api/v2";
            } else {
                this.url = "https://api.crowdin.com/api/v2";
            }
        }
    }
}
