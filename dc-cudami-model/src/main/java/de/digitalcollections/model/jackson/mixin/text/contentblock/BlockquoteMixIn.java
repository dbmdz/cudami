package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Blockquote;

@JsonDeserialize(as = Blockquote.class)
public interface BlockquoteMixIn extends ContentBlockNodeMixin {}
