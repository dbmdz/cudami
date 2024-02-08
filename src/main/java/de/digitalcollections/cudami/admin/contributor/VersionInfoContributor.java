package de.digitalcollections.cudami.admin.contributor;

import de.digitalcollections.cudami.admin.model.monitoring.VersionInfo;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class VersionInfoContributor implements InfoContributor {

  private final VersionInfo versionInfo;

  public VersionInfoContributor(VersionInfo versionInfo) {
    this.versionInfo = versionInfo;
  }

  @Override
  public void contribute(Info.Builder bldr) {
    bldr.withDetail("version", versionInfo.getArtifactVersions());
  }
}
