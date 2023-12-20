package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.BulletList;

@JsonDeserialize(as = BulletList.class)
public interface BulletListMixIn extends ContentBlockNodeMixin {}
