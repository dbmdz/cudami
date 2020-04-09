package de.digitalcollections.cudami.server.backend.impl.database.migration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class V1_6_3__DML_Change_fileresource_filename_to_resourceTest {

  @Test
  public void testConvertUri() throws Exception {
    V1_6_3__DML_Change_fileresource_filename_to_resource instance =
        new V1_6_3__DML_Change_fileresource_filename_to_resource();
    String oldUri =
        "file:///home/ralf/cudami/fileResources/image/jpeg/bb1a/a576/1145/412a/b364/4a72/705a/3ae7/bb1aa576-1145-412a-b364-4a72705a3ae7.jpg";
    String newUri = instance.convertUri(oldUri);
    assertEquals(
        "file:///home/ralf/cudami/fileResources/image/jpeg/bb1a/a576/1145/412a/b364/4a72/705a/3ae7/resource.jpg",
        newUri);

    oldUri =
        "file:///home/ralf/cudami/fileResources/application/undefined/bb1a/a576/1145/412a/b364/4a72/705a/3ae7/bb1aa576-1145-412a-b364-4a72705a3ae7";
    newUri = instance.convertUri(oldUri);
    assertEquals(
        "file:///home/ralf/cudami/fileResources/application/undefined/bb1a/a576/1145/412a/b364/4a72/705a/3ae7/resource",
        newUri);
  }
}
