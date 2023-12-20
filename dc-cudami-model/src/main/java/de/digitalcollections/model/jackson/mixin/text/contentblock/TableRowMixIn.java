package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.TableRow;

@JsonDeserialize(as = TableRow.class)
public interface TableRowMixIn extends ContentBlockNodeMixin {}
