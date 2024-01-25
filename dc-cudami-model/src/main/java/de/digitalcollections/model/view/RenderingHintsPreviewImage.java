package de.digitalcollections.model.view;

import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URL;
import java.util.Objects;

/**
 * Contains hints for rendering a preview image, e.g. in a webpage as HTML.<br>
 * These are related to an {@link ImageFileResource} but there may exist more than one rendering
 * hints container, each per individual use case.<br>
 * This makes it possible to use/embed an image in several projects and locations, each time
 * rendered individually.<br>
 * The rendering hints therefore are kept separate in an own rendering hints object and have to be
 * stored in conjunction with each specific use case.
 *
 * <p>Example:<br>
 * A person object has a portrait photo as preview image. On the detail page of this person (= use
 * case) specific caption and link are defined.<br>
 * So at the person storage an {@link ImageFileResource} is defined as preview image and individual
 * filled rendering hints are stored beside at the person object (and not "centrally" at the
 * reusable {@link ImageFileResource}).
 *
 * <p>This makes it possible to reuse an image in different use cases but with individual alt-text,
 * caption, (mouseover) title and link.
 */
public class RenderingHintsPreviewImage {

  private LocalizedText altText;
  private LocalizedText caption;
  private boolean openLinkInNewWindow;
  private URL targetLink;
  private LocalizedText title;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RenderingHintsPreviewImage)) {
      return false;
    }
    RenderingHintsPreviewImage that = (RenderingHintsPreviewImage) o;
    return openLinkInNewWindow == that.openLinkInNewWindow
        && Objects.equals(altText, that.altText)
        && Objects.equals(caption, that.caption)
        && Objects.equals(targetLink, that.targetLink)
        && Objects.equals(title, that.title);
  }

  /**
   * @return localized text that is shown as alternative if image can not be shown and for
   *     accessibility (e.g. screen-reader)
   */
  public LocalizedText getAltText() {
    return altText;
  }

  /**
   * @return localized text that may be shown e.g. as "subtitle" under an image
   */
  public LocalizedText getCaption() {
    return caption;
  }

  /**
   * @return url that is linked with the image and/or caption, e.g. used for click on image/caption
   *     as target location
   */
  public URL getTargetLink() {
    return targetLink;
  }

  /**
   * @return localized text that may be shown e.g. as "mouseover" if image is rendered in an HTML
   *     page
   */
  public LocalizedText getTitle() {
    return title;
  }

  @Override
  public int hashCode() {
    return Objects.hash(altText, caption, openLinkInNewWindow, targetLink, title);
  }

  /**
   * @return if targetLink should be opened in new window
   */
  public boolean isOpenLinkInNewWindow() {
    return openLinkInNewWindow;
  }

  /**
   * @param altText localized text that is shown as alternative if image can not be shown and for
   *     accessibility (e.g. screen-reader)
   */
  public void setAltText(LocalizedText altText) {
    this.altText = altText;
  }

  /**
   * @param caption localized text that may be shown e.g. as "subtitle" under an image
   */
  public void setCaption(LocalizedText caption) {
    this.caption = caption;
  }

  /**
   * @param openLinkInNewWindow "true" if targetLink should be opened in new window
   */
  public void setOpenLinkInNewWindow(boolean openLinkInNewWindow) {
    this.openLinkInNewWindow = openLinkInNewWindow;
  }

  /**
   * @param targetLink url that is linked with the image and/or caption, e.g. used for click on
   *     image/caption as target location
   */
  public void setTargetLink(URL targetLink) {
    this.targetLink = targetLink;
  }

  /**
   * @param title localized text that may be shown e.g. as "mouseover" if image is rendered in an
   *     HTML page
   */
  public void setTitle(LocalizedText title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return "RenderingHintsPreviewImage{"
        + "altText="
        + altText
        + ", caption="
        + caption
        + ", openLinkInNewWindow="
        + openLinkInNewWindow
        + ", targetLink="
        + targetLink
        + ", title="
        + title
        + '}';
  }
}
