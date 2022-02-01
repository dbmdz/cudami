package de.digitalcollections.cudami.server.controller.editor;

import java.beans.PropertyEditorSupport;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class UrlEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    URL url = (URL) getValue();
    return URLEncoder.encode(url.toString(), StandardCharsets.UTF_8);
  }

  @Override
  public void setAsText(String urlParamValue) {
    try {
      String decodedUrlParamValue = URLDecoder.decode(urlParamValue, StandardCharsets.UTF_8);
      URL value = URI.create(decodedUrlParamValue).toURL();
      setValue(value);
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
