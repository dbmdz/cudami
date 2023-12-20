package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Table;

@JsonDeserialize(as = Table.class)
public interface TableMixIn extends ContentBlockNodeMixin {}
