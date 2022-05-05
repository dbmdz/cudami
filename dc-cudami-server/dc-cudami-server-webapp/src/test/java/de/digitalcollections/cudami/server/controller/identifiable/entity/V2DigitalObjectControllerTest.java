package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2DigitalObjectController.class)
@DisplayName("The DigitalObjectController V2")
public class V2DigitalObjectControllerTest extends BaseControllerTest {

  @MockBean private DigitalObjectService digitalObjectService;

  @DisplayName("returns a paged list of digital objects")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/digitalobjects?pageNumber=0&pageSize=1"})
  public void getPagedList(String path) throws Exception {
    PageResponse<DigitalObject> expected =
        PageResponse.builder()
            .forPageSize(1)
            .forAscendingOrderedField("label", "de")
            .forAscendingOrderedField("label")
            .withTotalElements(1305745)
            .withContent(
                DigitalObject.builder()
                    .uuid("3d01a4b5-deb9-4d04-a1ee-9f9ac16f510f")
                    .created("2020-09-29T12:06:32.360852")
                    .identifier("mdz-obj", "bsb00041120", "ec6994d1-c248-4aa6-b5b1-b785cdcaf91d")
                    .label("08/15 : ein Standard des 20. Jahrhunderts")
                    .lastModified("2020-10-20T14:44:20.995665")
                    .previewImage(
                        "default.jpg",
                        "06b3122c-490e-4648-9195-cc290057ec3d",
                        "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00041120_00003/full/250,/0/default.jpg",
                        MimeType.MIME_IMAGE,
                        "https://api-dev.digitale-sammlungen.de/iiif/image/v2/bsb00041120_00003")
                    .customAttribute("showDfg", true)
                    .refId(37343)
                    .build())
            .build();

    when(digitalObjectService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v2/digitalobjects/list.json");
  }
}
