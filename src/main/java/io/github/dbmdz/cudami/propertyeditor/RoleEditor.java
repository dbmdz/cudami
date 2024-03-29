package io.github.dbmdz.cudami.propertyeditor;

import de.digitalcollections.model.security.Role;
import java.beans.PropertyEditorSupport;
import org.springframework.stereotype.Component;

@Component
public class RoleEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    Role role = (Role) getValue();
    return role.name();
  }

  @Override
  public void setAsText(String roleName) {
    setValue(Role.valueOf(roleName));
  }
}
