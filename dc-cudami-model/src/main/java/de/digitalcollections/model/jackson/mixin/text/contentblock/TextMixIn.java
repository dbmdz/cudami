package de.digitalcollections.model.jackson.mixin.text.contentblock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.List;

@JsonDeserialize(as = Text.class)
public interface TextMixIn {

  @JsonProperty("marks")
  List<Mark> getMarks();

  @JsonProperty("marks")
  void setMarks(List<Mark> marks);

  @JsonIgnore
  void addMark(Mark mark);
}
