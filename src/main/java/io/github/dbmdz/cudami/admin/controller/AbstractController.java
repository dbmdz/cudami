package io.github.dbmdz.cudami.admin.controller;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

public abstract class AbstractController {

  protected void verifyBinding(BindingResult br) {
    String[] suppressedFields = br.getSuppressedFields();
    if (suppressedFields.length > 0) {
      throw new RuntimeException(
          "Attempting to bind suppressed fields: "
              + StringUtils.arrayToCommaDelimitedString(suppressedFields));
    }
  }
}
