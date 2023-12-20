package de.digitalcollections.model.jackson.identifiable.entity;

import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class ProjectTest extends BaseJsonSerializationTest {

  private Project createObject() {
    Project project = new Project();
    project.setLabel(new LocalizedText(Locale.GERMAN, "Projekt XY"));
    project.setStartDate(LocalDate.of(2017, 06, 16));
    project.setStartDate(LocalDate.of(2019, 07, 31));
    return project;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Project project = createObject();
    checkSerializeDeserialize(project, "serializedTestObjects/identifiable/entity/Project.json");
  }
}
