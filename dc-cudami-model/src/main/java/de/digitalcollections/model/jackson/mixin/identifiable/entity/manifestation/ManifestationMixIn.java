package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;

@JsonDeserialize(as = Manifestation.class)
@JsonTypeName("MANIFESTATION")
public interface ManifestationMixIn extends EntityMixIn {}
