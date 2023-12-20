package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

/** Article is used to manage cultural articles and their hierarchy. */
@SuperBuilder(buildMethodName = "prebuild")
public class Article extends Entity {

  private List<Agent> creators;
  private LocalDate datePublished;
  private LocalizedStructuredContent text;
  private TimeValue timeValuePublished;

  public Article() {
    super();
  }

  public List<Agent> getCreators() {
    return creators;
  }

  public LocalDate getDatePublished() {
    return datePublished;
  }

  public LocalizedStructuredContent getText() {
    return text;
  }

  public TimeValue getTimeValuePublished() {
    return timeValuePublished;
  }

  @Override
  protected void init() {
    super.init();
    if (creators == null) {
      creators = new ArrayList<>(0);
    }
  }

  public void setCreators(List<Agent> creators) {
    this.creators = creators;
  }

  public void setDatePublished(LocalDate datePublished) {
    this.datePublished = datePublished;
  }

  public void setText(LocalizedStructuredContent text) {
    this.text = text;
  }

  public void setTimeValuePublished(TimeValue timeValuePublished) {
    this.timeValuePublished = timeValuePublished;
  }

  public abstract static class ArticleBuilder<C extends Article, B extends ArticleBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }
  }
}
