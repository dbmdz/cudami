package de.digitalcollections.cudami.admin.business.io;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class FileService {
  public static String bytesToSize(long sizeInBytes) {
    if (sizeInBytes == 0) {
      return "n/a";
    }
    return FileUtils.byteCountToDisplaySize(sizeInBytes);
  }
}
