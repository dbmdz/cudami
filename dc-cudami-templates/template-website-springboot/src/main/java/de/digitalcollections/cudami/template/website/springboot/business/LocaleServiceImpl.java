package de.digitalcollections.cudami.template.website.springboot.business;

import de.digitalcollections.cudami.template.website.springboot.repository.LocaleRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocaleServiceImpl implements LocaleService {

  @Autowired
  private LocaleRepository repository;

  @Override
  public Locale getDefaultLocale() {
    return repository.getDefault();
  }

}
