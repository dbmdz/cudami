package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.text.LocalizedStructuredContent;
import java.time.LocalDate;
import lombok.experimental.SuperBuilder;

/**
 * Project is used to describe a project (like a digitization project or an electronic publishing
 * project). See also https://schema.org/Project (Thing - Organization - Project)
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Project extends Entity {

  private LocalDate endDate;
  private LocalDate startDate;
  private LocalizedStructuredContent text;

  public Project() {
    super();
  }

  /**
   * @return date when project ended (null if still running)
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * @return date when project was started
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * @return localized formatted text describing project
   */
  public LocalizedStructuredContent getText() {
    return text;
  }

  @Override
  protected void init() {
    super.init();
  }

  /**
   * @param endDate set date when project ended
   */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /**
   * @param startDate set date when project starts/started (may be in the future)
   */
  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  /**
   * @param text set localized formatted text describing project
   */
  public void setText(LocalizedStructuredContent text) {
    this.text = text;
  }

  public abstract static class ProjectBuilder<C extends Project, B extends ProjectBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }
}
