package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Mark;

@JsonDeserialize(as = Mark.class)
public interface MarkMixIn extends ContentBlockWithAttributesMixIn {}
