package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.Thumbnail;
import de.digitalcollections.cudami.model.api.Translation;
import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.model.api.security.User;
import de.digitalcollections.cudami.model.jackson.mixin.TextMixIn;
import de.digitalcollections.cudami.model.jackson.mixin.ThumbnailMixIn;
import de.digitalcollections.cudami.model.jackson.mixin.TranslationMixIn;
import de.digitalcollections.cudami.model.jackson.mixin.entity.ContentTreeMixIn;
import de.digitalcollections.cudami.model.jackson.mixin.entity.WebsiteMixIn;
import de.digitalcollections.cudami.model.jackson.mixin.security.UserMixIn;
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
//    context.setMixInAnnotations(Entity.class, EntityMixIn.class); // FIXME not needed/working, switched back to wrapper info...
    context.setMixInAnnotations(ContentTree.class, ContentTreeMixIn.class);
    context.setMixInAnnotations(Text.class, TextMixIn.class);
    context.setMixInAnnotations(Thumbnail.class, ThumbnailMixIn.class);
    context.setMixInAnnotations(Translation.class, TranslationMixIn.class);
    context.setMixInAnnotations(User.class, UserMixIn.class);
    context.setMixInAnnotations(Website.class, WebsiteMixIn.class);
  }

  @Override
  public Version version() {
    return new Version(1, 0, 0, "SNAPSHOT", "de.digitalcollections.cudami", "model-jackson");
  }

}
