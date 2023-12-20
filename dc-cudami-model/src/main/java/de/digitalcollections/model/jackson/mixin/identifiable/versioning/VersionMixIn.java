package de.digitalcollections.model.jackson.mixin.identifiable.versioning;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.versioning.Version;

@JsonDeserialize(as = Version.class)
public interface VersionMixIn {}
