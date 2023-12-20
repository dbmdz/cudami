package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Video;

@JsonDeserialize(as = Video.class)
public interface VideoMixIn extends ContentBlockWithAttributesMixIn {}
