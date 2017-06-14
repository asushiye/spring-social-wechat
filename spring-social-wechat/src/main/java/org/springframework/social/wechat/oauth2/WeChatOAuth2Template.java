package org.springframework.social.wechat.oauth2;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.support.FormMapHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

public class WeChatOAuth2Template extends OAuth2Template {

	private String appid;
	private String appsecret;

	public WeChatOAuth2Template(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
		super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
		try {
			Field authorizeUrlField=OAuth2Template.class.getDeclaredField("authorizeUrl");
			authorizeUrlField.setAccessible(true);
			String authorizeUrlValue = (String) authorizeUrlField.get(this);
			authorizeUrlField.set(this,authorizeUrlValue.replace("?client_id","?appid"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.appid = clientId;
		this.appsecret=clientSecret;
	}

	public WeChatOAuth2Template(String clientId, String clientSecret, String authorizeUrl,String authenticateUrl, String accessTokenUrl) {
		super(clientId, clientSecret, authorizeUrl ,authenticateUrl , accessTokenUrl);
		try {
			Field authorizeUrlField=OAuth2Template.class.getDeclaredField("authorizeUrl");
			authorizeUrlField.setAccessible(true);
			String authorizeUrlValue = (String) authorizeUrlField.get(this);
			authorizeUrlField.set(this,authorizeUrlValue.replace("?client_id","?appid"));

			Field authenticateUrlField=OAuth2Template.class.getDeclaredField("authenticateUrl");
			authenticateUrlField.setAccessible(true);
			String authenticateUrlValue = (String) authenticateUrlField.get(this);
			authenticateUrlField.set(this,authenticateUrlValue.replace("?client_id","?appid"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.appid = clientId;
		this.appsecret=clientSecret;
	}

	private String getAuthorizeUrl(){
		Field authorizeUrlField= null;
		String authorizeUrlValue =null;
		try {
			authorizeUrlField = OAuth2Template.class.getDeclaredField("authorizeUrl");
			authorizeUrlField.setAccessible(true);
			authorizeUrlValue = (String) authorizeUrlField.get(this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return authorizeUrlValue;

	}

	private String getAuthenticateUrl(){
		Field authenticateUrlField= null;
		String authenticateUrlValue =null;
		try {
			authenticateUrlField = OAuth2Template.class.getDeclaredField("authenticateUrl");
			authenticateUrlField.setAccessible(true);
			authenticateUrlValue = (String) authenticateUrlField.get(this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return authenticateUrlValue;

	}

	public String buildAuthenticateUrl(OAuth2Parameters parameters) {
		return getAuthenticateUrl() != null ? buildAuthUrl(getAuthenticateUrl(), GrantType.AUTHORIZATION_CODE, parameters) : buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, parameters);
	}

	public String buildAuthenticateUrl(GrantType grantType, OAuth2Parameters parameters) {
		return getAuthenticateUrl() != null ? buildAuthUrl(getAuthenticateUrl(), grantType, parameters) : buildAuthorizeUrl(grantType, parameters);
	}

	public String buildAuthorizeUrl(OAuth2Parameters parameters) {
		return buildAuthUrl(getAuthorizeUrl(), GrantType.AUTHORIZATION_CODE, parameters);
	}

	public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
		return buildAuthUrl(getAuthorizeUrl(), grantType, parameters);
	}


	private String buildAuthUrl(String baseAuthUrl, GrantType grantType, OAuth2Parameters parameters) {
		StringBuilder authUrl = new StringBuilder(baseAuthUrl);
		List<String> redirectUri = parameters.remove("redirect_uri");
		authUrl.append('&').append("redirect_uri").append('=').append(redirectUri.get(0));
		if (grantType == GrantType.AUTHORIZATION_CODE) {
			authUrl.append('&').append("response_type").append('=').append("code");
		} else if (grantType == GrantType.IMPLICIT_GRANT) {
			authUrl.append('&').append("response_type").append('=').append("token");
		}
		for (Iterator<Map.Entry<String, List<String>>> additionalParams = parameters.entrySet().iterator(); additionalParams.hasNext();) {
			Map.Entry<String, List<String>> param = additionalParams.next();
			String name = formEncode(param.getKey());
			for (Iterator<String> values = param.getValue().iterator(); values.hasNext();) {
				authUrl.append('&').append(name);
				String value = values.next();
				if (StringUtils.hasLength(value)) {
					authUrl.append('=').append(formEncode(value));
				}
			}
		}

		return authUrl.toString();
	}

	protected AccessGrant createAccessGrant(String accessToken, String scope, String refreshToken, Long expiresIn, Map<String, Object> response) {
		String openid = (String) response.get("openid");
		return new WeChatAccessGrant(accessToken, scope, refreshToken, expiresIn, openid);
	}

	@Override
	protected AccessGrant postForAccessGrant(String accessTokenUrl, MultiValueMap<String, String> parameters) {
		parameters.add("appid", appid);
		parameters.add("secret", appsecret );
		return super.postForAccessGrant(accessTokenUrl, parameters);
	}

	protected RestTemplate getRestTemplate() {
		final RestTemplate restTemplate = super.getRestTemplate();
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(2);
		converters.add(new FormHttpMessageConverter());
		converters.add(new FormMapHttpMessageConverter());
		//converters.add(new MappingJackson2HttpMessageConverter());
		List<MediaType> mediaTypes = new ArrayList<MediaType>(2);
		mediaTypes.add(MediaType.APPLICATION_JSON);
		mediaTypes.add(MediaType.TEXT_PLAIN);
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(mediaTypes);
		converters.add(converter);
		converters.add(new StringHttpMessageConverter());



		restTemplate.setMessageConverters(converters);

		return  restTemplate;
	}


	private String formEncode(String data) {
		try {
			return URLEncoder.encode(data, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			// should not happen, UTF-8 is always supported
			throw new IllegalStateException(ex);
		}
	}
}
