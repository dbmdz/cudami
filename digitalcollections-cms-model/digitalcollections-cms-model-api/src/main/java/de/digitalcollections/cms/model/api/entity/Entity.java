package de.digitalcollections.cms.model.api.entity;

import de.digitalcollections.cms.model.api.Text;

public interface Entity {

  Text getLabel();

  void setLabel(Text label);

  String getUuid();

  void setUuid(String uuid);
}
