package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Image;

@JsonDeserialize(as = Image.class)
public interface ImageMixIn extends ContentBlockWithAttributesMixIn {}
