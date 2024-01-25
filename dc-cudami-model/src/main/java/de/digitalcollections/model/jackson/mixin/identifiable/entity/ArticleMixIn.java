package de.digitalcollections.model.jackson.mixin.identifiable.entity;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.entity.Article;

@JsonDeserialize(as = Article.class)
@JsonTypeName("ARTICLE")
public interface ArticleMixIn extends EntityMixIn {}
