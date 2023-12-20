package de.digitalcollections.model.jackson.identifiable.entity.work;

import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.time.TimeValue;
import java.time.LocalDate;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class WorkTest extends BaseJsonSerializationTest {

  private Work createObject() {
    Work work = new Work();
    work.setLabel(new LocalizedText(Locale.GERMAN, "Zimmer-Gymnastik ohne Geräte"));
    Person person = new Person();
    person.setLabel(new LocalizedText(Locale.GERMAN, "Arnold Hiller"));
    work.setFirstAppearedDate(LocalDate.parse("2020-04-28"));
    TimeValue timeValuePublished =
        new TimeValue(
            2020, 0, 0, 0, 0, 0, TimeValue.PREC_YEAR, 0, 0, 0, TimeValue.CM_GREGORIAN_PRO);
    work.setFirstAppearedTimeValue(timeValuePublished);
    return work;
  }

  @Test
  public void testSerializeDeserialize() throws Exception {
    Work work = createObject();
    checkSerializeDeserialize(work, "serializedTestObjects/identifiable/entity/work/Work.json");
  }
}
