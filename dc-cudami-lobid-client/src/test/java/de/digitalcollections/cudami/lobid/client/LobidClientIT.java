package de.digitalcollections.cudami.lobid.client;

import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import org.junit.jupiter.api.Test;

public class LobidClientIT {

  public LobidClientIT() {}

  @Test
  public void getCorporationByGndId() throws HttpException {
    Corporation corporation = new LobidClient().forCorporations().getByGndId("2007744-0");
    
    System.out.println("test");
  }
}
