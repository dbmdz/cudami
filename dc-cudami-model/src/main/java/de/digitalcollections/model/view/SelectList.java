package de.digitalcollections.model.view;

import java.util.LinkedList;
import java.util.List;

/** A List with a title, selectable options and one dedicated option, which is selected. */
public class SelectList {

  List<SelectOption> options = new LinkedList<>();
  SelectOption selectedOption;
  String title;

  public void addOption(SelectOption option) {
    options.add(option);
  }

  public List<SelectOption> getOptions() {
    return options;
  }

  public SelectOption getSelectedOption() {
    return selectedOption;
  }

  public String getTitle() {
    return title;
  }

  public void setOptions(List<SelectOption> options) {
    if (options != null) {
      this.options = options;
    }
  }

  public void setSelectedOption(SelectOption selectedOption) {
    this.selectedOption = selectedOption;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{"
        + "title='"
        + title
        + "', selectedOption="
        + selectedOption
        + ", options="
        + options
        + "}";
  }
}
