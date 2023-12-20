package de.digitalcollections.model.identifiable.entity.manifestation;

import lombok.experimental.SuperBuilder;

@SuperBuilder(buildMethodName = "prebuild")
public class DistributionInfo extends PublishingInfo {

  public DistributionInfo() {
    super();
  }

  public abstract static class DistributionInfoBuilder<
          C extends DistributionInfo, B extends DistributionInfoBuilder<C, B>>
      extends PublishingInfoBuilder<C, B> {
    public C build() {
      C c = prebuild();
      return c;
    }
  }
}
