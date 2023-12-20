package de.digitalcollections.model.jackson.mixin.identifiable.semantic;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;

@JsonDeserialize(as = Subject.class)
@JsonTypeName("SUBJECT")
public interface SubjectMixIn extends IdentifiableMixIn {}
