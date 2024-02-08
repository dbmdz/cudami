package de.digitalcollections.cudami.admin.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.admin.model.monitoring.VersionInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {VersionInfo.class, VersionActuatorEndpoint.class})
@SpringBootConfiguration()
class VersionActuatorEndpointTest {

  @Autowired VersionActuatorEndpoint versionActuatorEndpoint;

  @Test
  @DisplayName("Test for return value of the /version endpoint")
  public void testGetVersion() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String jsonResult = mapper.writeValueAsString(versionActuatorEndpoint.getVersion());

    assertThat(jsonResult)
        .isEqualTo(
            "{\"name\":\"cudami-admin-webapp\",\"version\":\"1.2.3\",\"details\":\"build by foo@bar.com\"}");
  }
}
