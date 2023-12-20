package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Paragraph;

@JsonDeserialize(as = Paragraph.class)
public interface ParagraphMixIn extends ContentBlockNodeMixin {}
