package de.digitalcollections.model.jackson.mixin.view;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.view.RenderingHints;

@JsonDeserialize(as = RenderingHints.class)
@JsonTypeName("RENDERING_HINTS")
public class RenderingHintsMixIn {}
