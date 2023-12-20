package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.Website;

@JsonDeserialize(as = Website.class)
@JsonTypeName("WEBSITE")
public interface WebsiteMixIn extends EntityMixIn {}
