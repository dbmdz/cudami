package de.digitalcollections.cudami.server.model;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This class holds common helpers and other stuff, which are used in multiple builders */
public abstract class AbstractBuilder {

  protected UUID extractFirstUuidFromPath(String path) {
    Pattern uuidPattern =
        Pattern.compile("(\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12})");
    Matcher matcher = uuidPattern.matcher(path);
    if (matcher.find()) {
      return UUID.fromString(matcher.group(0));
    }
    return null;
  }
}
