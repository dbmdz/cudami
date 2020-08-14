package de.digitalcollections.cudami.admin.util;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class FileHelper {
  public static String bytesToSize(long sizeInBytes) {
    if (sizeInBytes == 0) {
      return "n/a";
    }
    return FileUtils.byteCountToDisplaySize(sizeInBytes);
  }
}
