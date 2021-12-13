package de.digitalcollections.cudami.server.config;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(
    proxyBeanMethods = false) // We want the real configuration class, not the cglib proxy
@ConfigurationProperties(prefix = "cudami")
public class CudamiServerConfig extends CudamiConfig {}
