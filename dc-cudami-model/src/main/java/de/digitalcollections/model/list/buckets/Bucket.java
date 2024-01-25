package de.digitalcollections.model.list.buckets;

import de.digitalcollections.model.UniqueObject;

public class Bucket<T extends UniqueObject> {

  private T endObject;

  private T startObject;

  public Bucket() {
    this.endObject = null;
    this.startObject = null;
  }

  public Bucket(T startObject, T endObject) {
    this.startObject = startObject;
    this.endObject = endObject;
  }

  public T getEndObject() {
    return endObject;
  }

  public T getStartObject() {
    return startObject;
  }

  public void setEndObject(T endObject) {
    this.endObject = endObject;
  }

  public void setStartObject(T startObject) {
    this.startObject = startObject;
  }
}
