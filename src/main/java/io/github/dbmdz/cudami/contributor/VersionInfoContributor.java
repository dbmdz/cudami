package io.github.dbmdz.cudami.contributor;

import io.github.dbmdz.cudami.model.monitoring.VersionInfo;
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
