package org.springframework.social.wechat.api.impl;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.HttpRequestDecorator;
import org.springframework.social.wechat.api.UserOperations;
import org.springframework.social.wechat.api.WeChat;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class WeChatTemplate extends AbstractOAuth2ApiBinding implements WeChat {
	
	private String openid;
	private String accessToken;
	private UserOperations userOperations;
	
    public WeChatTemplate(String accessToken, String openid) {
        super(accessToken);
        this.openid = openid;
        this.accessToken = accessToken;
        initialize();
    }

	public UserOperations userOperations() {
		return userOperations;
	}

    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(requestFactory));
    }

    @Override
    protected MappingJackson2HttpMessageConverter getJsonMessageConverter() {
        List<MediaType> mediaTypes = new ArrayList<MediaType>(2);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.TEXT_PLAIN);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(mediaTypes);

        return converter;
    }

    private void initialize() {
        registerOAuth2Interceptor(accessToken);
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(this.getRestTemplate().getRequestFactory()));
        this.initSubApis();

    }

    private void initSubApis() {
        this.userOperations = new UserTemplate(getRestTemplate(), this.openid, this.accessToken);
    }

    private void registerOAuth2Interceptor(String accessToken) {
        List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();
        interceptors.add(new OAuth2TokenParameterRequestInterceptor(accessToken));
        getRestTemplate().setInterceptors(interceptors);
    }


    private static final class OAuth2TokenParameterRequestInterceptor implements ClientHttpRequestInterceptor {
        private final String accessToken;

        public OAuth2TokenParameterRequestInterceptor(String accessToken) {
            this.accessToken = accessToken;
        }

        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, ClientHttpRequestExecution execution) throws IOException {
            HttpRequest protectedResourceRequest = new HttpRequestDecorator(request) {
                @Override
                public URI getURI() {
                    return URI.create(super.getURI().toString() + (((super.getURI().getQuery() == null) ? "?" : "&") + "access_token=" + accessToken));
                }
            };
            return execution.execute(protectedResourceRequest, body);
        }

    }

}
