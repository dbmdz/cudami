package de.digitalcollections.cudami.lobid.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.api.http.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import org.junit.jupiter.api.Test;

public class LobidClientIT {

  public LobidClientIT() {}

  @Test
  public void getCorporateBodyByGndId() throws HttpException {
    CorporateBody corporateBody = new LobidClient().forCorporateBodies().getByGndId("2007744-0");
    assertEquals("Deutsche Forschungsgemeinschaft (DFG)", corporateBody.getLabel().getText());
    assertEquals("https://www.dfg.de/", corporateBody.getHomepageUrl().toString());
    assertEquals("2007744-0", corporateBody.getIdentifierByNamespace("gnd").getId());
  }
}
