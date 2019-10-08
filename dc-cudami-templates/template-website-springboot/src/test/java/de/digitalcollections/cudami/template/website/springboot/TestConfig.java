package de.digitalcollections.cudami.template.website.springboot;

import de.digitalcollections.cudami.template.website.springboot.repository.LocaleRepository;
import java.util.Locale;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

  @Bean
  @Primary
  public LocaleRepository localeRepository() {
    final LocaleRepository dummy = Mockito.mock(LocaleRepository.class);
    Locale defaultLanguage = new Locale("en");

    Mockito.when(dummy.getDefault()).thenReturn(defaultLanguage);
    return dummy;
  }
}
