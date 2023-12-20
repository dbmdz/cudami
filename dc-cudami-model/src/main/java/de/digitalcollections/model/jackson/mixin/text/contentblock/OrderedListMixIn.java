package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.OrderedList;

@JsonDeserialize(as = OrderedList.class)
public interface OrderedListMixIn extends ContentBlockNodeMixin, ContentBlockWithAttributesMixIn {}
