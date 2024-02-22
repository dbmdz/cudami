package io.github.dbmdz.cudami.admin.model.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(versionInfo.getApplicationName()).isEqualTo("cudami");
    assertThat(versionInfo.getVersionInfo()).isEqualTo("1.2.3");
    assertThat(versionInfo.getBuildDetails()).isEqualTo("build by foo@bar.com");
  }
}
