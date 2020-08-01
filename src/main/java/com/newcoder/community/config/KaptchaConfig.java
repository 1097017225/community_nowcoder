package com.newcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {

        Properties prop = new Properties();
        prop.setProperty("kaptcha.image.width", "100");
        prop.setProperty("kaptcha.image.height", "40");
        prop.setProperty("kaptcha.textproducer.font.size", "32");
        prop.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        prop.setProperty("kaptcha.textproducer.char.string", "0123456789abcdefghijklmnopqrstuvwxyz");
        prop.setProperty("kaptcha.textproducer.char.length", "4");
        prop.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(prop);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
