package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;

@JsonDeserialize(as = HeadwordEntry.class)
@JsonTypeName("HEADWORD_ENTRY")
public interface HeadwordEntryMixIn extends EntityMixIn {}
