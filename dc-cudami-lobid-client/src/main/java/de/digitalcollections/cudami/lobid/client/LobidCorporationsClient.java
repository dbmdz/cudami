package de.digitalcollections.cudami.lobid.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;
import de.digitalcollections.cudami.lobid.client.model.LobidCorporateBody;
import de.digitalcollections.cudami.lobid.client.model.LobidHomepage;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobidCorporationsClient extends LobidBaseClient<LobidCorporateBody> {

  private final static Logger LOGGER = LoggerFactory.getLogger(LobidCorporationsClient.class);

  LobidCorporationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, LobidCorporateBody.class, mapper);
  }

  public Corporation getByGndId(String gndId) throws HttpException {
    LobidCorporateBody lobidCorporateBody
            = doGetRequestForObject(String.format("/gnd/%s.json", gndId));
    Corporation corporation = mapLobidToModel(lobidCorporateBody);
    return corporation;
  }

  private Corporation mapLobidToModel(LobidCorporateBody lobidCorporateBody) {
    Corporation corporation = new CorporationImpl();

    // label
    String labelText = lobidCorporateBody.getPreferredName();
    List<String> abbreviatedNameForTheCorporateBody = lobidCorporateBody.getAbbreviatedNameForTheCorporateBody();
    if (labelText != null && abbreviatedNameForTheCorporateBody != null) {
      labelText = labelText + " (" + abbreviatedNameForTheCorporateBody.get(0)
              + ")";
    }
    corporation.setLabel(new LocalizedTextImpl(Locale.GERMAN, labelText));

    // identifier
    String gndIdentifier = lobidCorporateBody.getGndIdentifier();
    if (gndIdentifier != null) {
      corporation.addIdentifier(new IdentifierImpl(null, "gnd", gndIdentifier));
    }

    // homepage
    List<LobidHomepage> homepage = lobidCorporateBody.getHomepage();
    if (homepage != null && !homepage.isEmpty()) {
      String homepageId = homepage.get(0).getId();
      try {
        corporation.setHomepageUrl(URI.create(homepageId).toURL());
      } catch (MalformedURLException ex) {
        LOGGER.warn("Invalid homepage URL: " + homepageId);
      }
    }
    
    // preview image = logo
    

    return corporation;
  }
}
