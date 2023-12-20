package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.CodeBlock;

@JsonDeserialize(as = CodeBlock.class)
public interface CodeBlockMixIn extends ContentBlockNodeMixin {}
