package de.digitalcollections.cudami.server.business.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.model.config.CudamiConfig.UrlAlias;
import de.digitalcollections.model.jackson.DigitalCollectionsObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractServiceImplTest {

  protected CudamiConfig cudamiConfig;

  private DigitalCollectionsObjectMapper mapper;

  protected <O> O createDeepCopy(O object) {
    try {
      String serializedObject = mapper.writeValueAsString(object);
      O copy = (O) mapper.readValue(serializedObject, object.getClass());

      assertThat(copy).isEqualTo(object);
      return copy;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot serialize/deserialize " + object + ": " + e, e);
    }
  }

  @BeforeEach
  protected void beforeEach() throws Exception {
    mapper = new DigitalCollectionsObjectMapper();

    cudamiConfig = mock(CudamiConfig.class);

    UrlAlias urlAliasConfig = mock(UrlAlias.class);
    when(cudamiConfig.getUrlAlias()).thenReturn(urlAliasConfig);

    when(urlAliasConfig.getGenerationExcludes())
        .thenReturn(List.of("DIGITALOBJECT", "ITEM", "MANIFESTATION", "WORK"));
  }
}
