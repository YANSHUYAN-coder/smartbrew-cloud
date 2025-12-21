package com.coffee.system.config;


import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfiguration {

    private String gatewayUrl;
    private String appId;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String charset;
    private String format;
    private String signType;

    @Bean
    public AlipayClient alipayClient() throws Exception {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(gatewayUrl);
        alipayConfig.setAppId(appId);
        alipayConfig.setPrivateKey(merchantPrivateKey);
        alipayConfig.setFormat(format);
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset(charset);
        alipayConfig.setSignType(signType);
        return new DefaultAlipayClient(alipayConfig);
    }
}