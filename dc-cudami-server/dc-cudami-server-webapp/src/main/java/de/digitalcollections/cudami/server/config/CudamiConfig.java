package de.digitalcollections.cudami.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(
    proxyBeanMethods = false) // We want the real configuration class, not the cglib proxy
@ConfigurationProperties(prefix = "cudami")
public class CudamiConfig extends de.digitalcollections.cudami.client.config.CudamiConfig {}
