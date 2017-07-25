package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.jackson.mixin.UserMixIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CudamiModule extends Module {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiModule.class);

  @Override
  public String getModuleName() {
    return "cudami jackson module";
  }

  @Override
  public void setupModule(SetupContext context) {
    LOGGER.info("Using CudamiModule");
    context.setMixInAnnotations(User.class, UserMixIn.class);
  }

  @Override
  public Version version() {
    return new Version(1, 0, 0, "SNAPSHOT", "de.digitalcollections.cudami", "model-jackson");
  }

}
