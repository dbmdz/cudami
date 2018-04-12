package de.digitalcollections.cudami.client.business;

import de.digitalcollections.cudami.client.repository.CudamiRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CudamiServiceImpl implements CudamiService {

  @Autowired
  private CudamiRepository repository;

  @Override
  public Locale getDefaultLocale() {
    return repository.getDefaultLocale();
  }

  @Override
  public List<Locale> getAllLocales() {
    return repository.getAllLocales();
  }
}
