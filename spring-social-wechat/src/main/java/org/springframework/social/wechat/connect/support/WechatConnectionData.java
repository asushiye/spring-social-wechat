package org.springframework.social.wechat.connect.support;

import org.springframework.social.connect.ConnectionData;

/**
 * Created by Alex on 2017/3/25.
 */
public class WechatConnectionData extends ConnectionData {
    private String openid;



    public WechatConnectionData(String providerId, String providerUserId, String displayName, String profileUrl, String imageUrl, String accessToken, String secret, String refreshToken, Long expireTime,String openid) {
        super(providerId, providerUserId, displayName, profileUrl, imageUrl, accessToken, secret, refreshToken, expireTime);
        this.openid=openid;
    }


    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
