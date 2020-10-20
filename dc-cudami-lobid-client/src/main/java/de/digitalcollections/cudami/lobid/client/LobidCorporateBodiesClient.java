package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.model.LobidCorporateBody;
import de.digitalcollections.cudami.lobid.client.model.LobidDepiction;
import de.digitalcollections.cudami.lobid.client.model.LobidHomepage;
import de.digitalcollections.model.api.http.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidCorporateBodiesClient extends LobidBaseClient<LobidCorporateBody> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobidCorporateBodiesClient.class);

  LobidCorporateBodiesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidCorporateBody.class, mapper);
  }

  public CorporateBody getByGndId(String gndId) throws HttpException {
    LobidCorporateBody lobidCorporateBody =
        doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    CorporateBody corporateBody = mapLobidToModel(lobidCorporateBody);
    return corporateBody;
  }

  private CorporateBody mapLobidToModel(LobidCorporateBody lobidCorporateBody) {
    CorporateBody corporateBody = new CorporateBodyImpl();

    // label
    String labelText = lobidCorporateBody.getPreferredName();
    List<String> abbreviatedNameForTheCorporateBody =
        lobidCorporateBody.getAbbreviatedNameForTheCorporateBody();
    if (labelText != null && abbreviatedNameForTheCorporateBody != null) {
      labelText = labelText + " (" + abbreviatedNameForTheCorporateBody.get(0) + ")";
    }
    corporateBody.setLabel(new LocalizedTextImpl(Locale.GERMAN, labelText));

    // identifier
    String gndIdentifier = lobidCorporateBody.getGndIdentifier();
    if (gndIdentifier != null) {
      corporateBody.addIdentifier(new IdentifierImpl(null, "gnd", gndIdentifier));
    }

    // homepage
    List<LobidHomepage> homepage = lobidCorporateBody.getHomepage();
    if (homepage != null && !homepage.isEmpty()) {
      String homepageId = homepage.get(0).getId();
      try {
        corporateBody.setHomepageUrl(URI.create(homepageId).toURL());
      } catch (MalformedURLException ex) {
        LOGGER.warn("Invalid homepage URL: " + homepageId);
      }
    }

    // preview image = logo
    List<LobidDepiction> depiction = lobidCorporateBody.getDepiction();
    if (depiction != null && !depiction.isEmpty()) {
      String thumbnailUrl = depiction.get(0).getThumbnail();
      ImageFileResource previewImage = new ImageFileResourceImpl();
      previewImage.setUri(URI.create(thumbnailUrl));
      previewImage.setMimeType(MimeType.MIME_IMAGE);
      corporateBody.setPreviewImage(previewImage);
    }

    return corporateBody;
  }
}
