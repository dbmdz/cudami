package de.digitalcollections.model.jackson.mixin.view;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;

@JsonDeserialize(as = RenderingHintsPreviewImage.class)
@JsonTypeName("RENDERING_HINTS_PREVIEW_IMAGE")
public interface RenderingHintsPreviewImageMixIn {}
