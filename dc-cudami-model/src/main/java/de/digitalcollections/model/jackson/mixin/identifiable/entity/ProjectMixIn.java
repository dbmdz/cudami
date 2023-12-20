package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.Project;

@JsonDeserialize(as = Project.class)
@JsonTypeName("PROJECT")
public interface ProjectMixIn extends EntityMixIn {}
