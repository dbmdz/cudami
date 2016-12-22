package de.digitalcollections.cms.model.api;

public interface Translation extends Entity {

  public String getText();

  public void setText(String text);

  public String getLang();

  public void setLang(String lang);

  public boolean has(String lang);

}
