package de.digitalcollections.cudami.server.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = VersionInfo.class)
@SpringBootConfiguration()
class VersionInfoTest {

  @Value("${junit.version}")
  private String junitVersion;

  @Autowired VersionInfo versionInfo;

  @Test
  @DisplayName("Testing application info from application.yml")
  void testApplicationInfoFromApplicationYml() {
    assertThat(versionInfo.getApplicationName())
        .isEqualTo("DigitalCollections: cudami Repository Server (Webapp)");
    assertThat(versionInfo.getVersionInfo()).isEqualTo("1.2.3");
    assertThat(versionInfo.getBuildDetails()).isEqualTo("build by foo@bar.com");
  }

  @Test
  @DisplayName("Testing dependency detail info")
  void testBuildDetails() {
    Map<String, String> versions = versionInfo.getArtifactVersions();

    String jarVersion = versions.get("junit-jupiter-" + junitVersion + ".jar");
    if (jarVersion != null) {
      // jar version can be null, if we have no jar artifact available
      assertThat(jarVersion).isEqualTo(junitVersion);
    }
  }
}
