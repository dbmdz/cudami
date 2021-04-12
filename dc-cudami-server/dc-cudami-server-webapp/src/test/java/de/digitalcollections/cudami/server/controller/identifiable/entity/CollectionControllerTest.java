package de.digitalcollections.cudami.server.controller.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(CollectionController.class)
@DisplayName("The CollectionController")
class CollectionControllerTest extends BaseControllerTest {

  @MockBean private CollectionService collectionService;
  @MockBean private LocaleService localeService;

  // TODO: Test latest/collections/<uuid>/subcollections with and without active flag

}
