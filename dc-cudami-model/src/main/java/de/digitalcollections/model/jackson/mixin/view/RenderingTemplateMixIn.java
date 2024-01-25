package de.digitalcollections.model.jackson.mixin.view;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.view.RenderingTemplate;

@JsonDeserialize(as = RenderingTemplate.class)
public interface RenderingTemplateMixIn extends UniqueObjectMixIn {}
