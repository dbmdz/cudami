package de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.MainSubTypeMixIn;
import de.digitalcollections.model.text.TitleType;

@JsonDeserialize(as = TitleType.class)
@JsonTypeName("TITLE_TYPE")
public interface TitleTypeMixIn extends MainSubTypeMixIn {}
