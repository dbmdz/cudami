package de.digitalcollections.model.view;

import java.io.Serializable;
import java.util.Objects;

/**
 * A rectangle (defined by its upper left and lower right coordinates) for highlighted text
 * including the text itself.
 */
public class HighlightingRectangle implements Serializable {

  private final double lrx;
  private final double lry;
  private final int ref;
  private final String text;
  private final double ulx;
  private final double uly;

  public HighlightingRectangle(
      String text, int ref, double ulx, double uly, double lrx, double lry) {
    this.ulx = ulx;
    this.uly = uly;
    this.lrx = lrx;
    this.lry = lry;
    this.ref = ref;
    this.text = text;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final HighlightingRectangle other = (HighlightingRectangle) obj;
    if (Double.doubleToLongBits(this.ulx) != Double.doubleToLongBits(other.getUlx())) {
      return false;
    }
    if (Double.doubleToLongBits(this.uly) != Double.doubleToLongBits(other.getUly())) {
      return false;
    }
    if (Double.doubleToLongBits(this.lrx) != Double.doubleToLongBits(other.getLrx())) {
      return false;
    }
    if (Double.doubleToLongBits(this.lry) != Double.doubleToLongBits(other.getLry())) {
      return false;
    }
    if (this.ref != other.getRef()) {
      return false;
    }
    return Objects.equals(this.text, other.getText());
  }

  /**
   * the relative abscissa of the lower right point of the rectangle
   *
   * @return a value in the range [0..1]
   */
  public double getLrx() {
    return lrx;
  }

  /**
   * the relative ordinate of the lower right point of the rectangle
   *
   * @return a value in the range [0..1]
   */
  public double getLry() {
    return lry;
  }

  /**
   * The reference of the highlight rectangle, e.g. a page number
   *
   * @return the value of the reference
   */
  public int getRef() {
    return ref;
  }

  /**
   * The text to be highlighted (e.g. for plaintext representation)
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * The relative abscissa (x) of the upper left point of the rectangle
   *
   * @return a value in the range [0..1]
   */
  public double getUlx() {
    return ulx;
  }

  /**
   * The relative ordinate (y) of the upper left point of the rectangle
   *
   * @return a value in the range [0..1]
   */
  public double getUly() {
    return uly;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash =
        31 * hash
            + (int)
                (Double.doubleToLongBits(this.ulx) ^ (Double.doubleToLongBits(this.ulx) >>> 32));
    hash =
        31 * hash
            + (int)
                (Double.doubleToLongBits(this.uly) ^ (Double.doubleToLongBits(this.uly) >>> 32));
    hash =
        31 * hash
            + (int)
                (Double.doubleToLongBits(this.lrx) ^ (Double.doubleToLongBits(this.lrx) >>> 32));
    hash =
        31 * hash
            + (int)
                (Double.doubleToLongBits(this.lry) ^ (Double.doubleToLongBits(this.lry) >>> 32));
    hash = 31 * hash + this.ref;
    hash = 31 * hash + Objects.hashCode(this.text);
    return hash;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{"
        + "text='"
        + text
        + '\''
        + ", ulx="
        + ulx
        + ", uly="
        + uly
        + ", lrx="
        + lrx
        + ", lry="
        + lry
        + ", ref="
        + ref
        + '}';
  }
}
