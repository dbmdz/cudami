package de.digitalcollections.cudami.admin.backend.api.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.feign.codec.EndpointErrorDecoder;
import feign.Feign;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Repository for Locale persistence handling. */
public interface LocaleRepository {

  @RequestLine("GET /latest/languages")
  List<String> findAllLanguages();

  @RequestLine("GET /latest/languages/default")
  Locale getDefaultLanguage();

  @RequestLine("GET /latest/locales")
  List<String> findAllLocales();

  @RequestLine("GET /latest/locales/default")
  String getDefaultLocale();

  @Configuration
  class Config {

    @Value(value = "${cudami.server.address}")
    private String cudamiServerAddress;

    @Autowired ObjectMapper objectMapper;

    @Bean
    public LocaleRepository localeRepository() {
      LocaleRepository endpoint =
          Feign.builder()
              .decoder(new JacksonDecoder(objectMapper))
              .encoder(new JacksonEncoder(objectMapper))
              .errorDecoder(new EndpointErrorDecoder())
              .target(LocaleRepository.class, cudamiServerAddress);
      return endpoint;
    }
  }
}
