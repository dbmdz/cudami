package de.digitalcollections.cudami.lobid.client.model;

import java.util.List;

public class LobidCorporateBody {

  private List<String> abbreviatedNameForTheCorporateBody;
  private List<LobidDepiction> depiction;
  private String gndIdentifier;
  private List<LobidHomepage> homepage;
  private String preferredName;

  public List<String> getAbbreviatedNameForTheCorporateBody() {
    return abbreviatedNameForTheCorporateBody;
  }

  public void setAbbreviatedNameForTheCorporateBody(
      List<String> abbreviatedNameForTheCorporateBody) {
    this.abbreviatedNameForTheCorporateBody = abbreviatedNameForTheCorporateBody;
  }

  public List<LobidDepiction> getDepiction() {
    return depiction;
  }

  public void setDepiction(List<LobidDepiction> depiction) {
    this.depiction = depiction;
  }

  public String getGndIdentifier() {
    return gndIdentifier;
  }

  public void setGndIdentifier(String gndIdentifier) {
    this.gndIdentifier = gndIdentifier;
  }

  public List<LobidHomepage> getHomepage() {
    return homepage;
  }

  public void setHomepage(List<LobidHomepage> homepage) {
    this.homepage = homepage;
  }

  public String getPreferredName() {
    return preferredName;
  }

  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }
}
