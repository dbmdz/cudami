package de.digitalcollections.cudami.client.feign;

public enum Environment {
  BDDTEST, TEST, LOCAL, DEVELOPMENT, STAGING, PRODUCTION;

  public static Environment fromString(String name) {
    String environmentName = name.toUpperCase();

    if ("DEV".equals(environmentName)) {
      return DEVELOPMENT;
    } else if ("STG".equals(environmentName)) {
      return STAGING;
    } else if ("PROD".equals(environmentName)) {
      return PRODUCTION;
    } else {
      return valueOf(environmentName);
    }
  }
}
