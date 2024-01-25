package de.digitalcollections.model.jackson.mixin.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY)
public interface TitleMixIn {}
