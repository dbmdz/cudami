package de.digitalcollections.cudami.model.api.identifiable.parts;

import java.util.Locale;

public interface Translation {

  public Locale getLocale();

  public void setLocale(Locale locale);

  public String getText();

  public void setText(String text);

  public boolean has(Locale locale);

}
