package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.TableHeader;

@JsonDeserialize(as = TableHeader.class)
public interface TableHeaderMixIn extends ContentBlockNodeMixin, ContentBlockWithAttributesMixIn {}
