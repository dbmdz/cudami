package de.digitalcollections.model.identifiable.entity.manifestation;

import lombok.experimental.SuperBuilder;

@SuperBuilder(buildMethodName = "prebuild")
public class ProductionInfo extends PublishingInfo {

  public ProductionInfo() {
    super();
  }

  public abstract static class ProductionInfoBuilder<
          C extends ProductionInfo, B extends ProductionInfoBuilder<C, B>>
      extends PublishingInfoBuilder<C, B> {

    public C build() {
      C c = prebuild();
      return c;
    }
  }
}
