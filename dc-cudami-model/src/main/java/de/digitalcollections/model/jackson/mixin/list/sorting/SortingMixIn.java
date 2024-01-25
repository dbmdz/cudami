package de.digitalcollections.model.jackson.mixin.list.sorting;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.list.sorting.Sorting;

@JsonDeserialize(as = Sorting.class)
public abstract class SortingMixIn {}
