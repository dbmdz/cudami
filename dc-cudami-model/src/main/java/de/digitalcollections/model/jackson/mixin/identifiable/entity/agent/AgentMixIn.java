package de.digitalcollections.model.jackson.mixin.identifiable.entity.agent;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "identifiableObjectType",
    visible = true)
@JsonSubTypes({
  // need to be uppercase (and included as EXISTING_PROPERTY) to reuse enum field values for
  // deserializing:
  @JsonSubTypes.Type(value = Agent.class, name = "AGENT"),
  @JsonSubTypes.Type(value = CorporateBody.class, name = "CORPORATE_BODY"),
  @JsonSubTypes.Type(value = Family.class, name = "FAMILY"),
  @JsonSubTypes.Type(value = Person.class, name = "PERSON")
})
public interface AgentMixIn extends EntityMixIn {}
