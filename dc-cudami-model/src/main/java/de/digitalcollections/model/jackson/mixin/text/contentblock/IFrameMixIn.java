package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.IFrame;

@JsonDeserialize(as = IFrame.class)
public interface IFrameMixIn extends ContentBlockWithAttributesMixIn {}
