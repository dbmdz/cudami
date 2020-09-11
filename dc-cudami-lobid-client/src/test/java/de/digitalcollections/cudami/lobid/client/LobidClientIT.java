package de.digitalcollections.cudami.lobid.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.digitalcollections.model.api.http.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import org.junit.jupiter.api.Test;

public class LobidClientIT {

  public LobidClientIT() {}

  @Test
  public void getCorporationByGndId() throws HttpException {
    Corporation corporation = new LobidClient().forCorporations().getByGndId("2007744-0");
    assertEquals("Deutsche Forschungsgemeinschaft (DFG)", corporation.getLabel().getText());
    assertEquals("https://www.dfg.de/", corporation.getHomepageUrl().toString());
    assertEquals("2007744-0", corporation.getIdentifierByNamespace("gnd").getId());
  }
}
