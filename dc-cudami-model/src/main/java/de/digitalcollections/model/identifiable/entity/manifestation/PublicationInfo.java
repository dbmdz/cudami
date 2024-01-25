package de.digitalcollections.model.identifiable.entity.manifestation;

import lombok.experimental.SuperBuilder;

@SuperBuilder(buildMethodName = "prebuild")
public class PublicationInfo extends PublishingInfo {

  public PublicationInfo() {
    super();
  }

  public abstract static class PublicationInfoBuilder<
          C extends PublicationInfo, B extends PublicationInfoBuilder<C, B>>
      extends PublishingInfoBuilder<C, B> {

    public C build() {
      C c = prebuild();
      return c;
    }
  }
}
