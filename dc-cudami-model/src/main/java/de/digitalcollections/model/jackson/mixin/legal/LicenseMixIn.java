package de.digitalcollections.model.jackson.mixin.legal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.legal.License;

@JsonDeserialize(as = License.class)
public interface LicenseMixIn extends UniqueObjectMixIn {}
