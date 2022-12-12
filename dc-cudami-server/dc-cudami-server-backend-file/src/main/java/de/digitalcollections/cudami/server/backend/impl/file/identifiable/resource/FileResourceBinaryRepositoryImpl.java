package de.digitalcollections.cudami.server.backend.impl.file.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Repository
public class FileResourceBinaryRepositoryImpl implements FileResourceBinaryRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceBinaryRepositoryImpl.class);
  public static final String DEFAULT_FILENAME_WITHOUT_EXTENSION = "resource";
  private final URL iiifImageBaseUrl;
  private final URL mediaVideoBaseUrl;
  private final String repositoryFolderPath;
  private final ResourceLoader resourceLoader;

  @Autowired
  public FileResourceBinaryRepositoryImpl(
      @Value("${cudami.repositoryFolderPath}") String folderPath,
      @Value("${iiif.image.baseUrl:#{null}}") URL iiifImageBaseUrl,
      @Value("${media.video.baseUrl:#{null}}") URL mediaVideoBaseUrl,
      ResourceLoader resourceLoader) {
    this.iiifImageBaseUrl = iiifImageBaseUrl;
    this.mediaVideoBaseUrl = mediaVideoBaseUrl;
    this.repositoryFolderPath = folderPath.replace("~", System.getProperty("user.home"));
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void assertReadability(FileResource resource)
      throws TechnicalException, ResourceNotFoundException {
    try (InputStream is = getInputStream(resource)) {
      if (is.available() <= 0) {
        throw new TechnicalException("Cannot read " + resource.getFilename() + ": Empty file");
      }
    } catch (TechnicalException e) {
      throw new TechnicalException("Cannot read " + resource.getFilename() + ": Empty file");
    } catch (ResourceNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new TechnicalException("Cannot read " + resource.getFilename() + ": " + e.getMessage());
    }
  }

  protected URI createUri(@NonNull UUID uuid, MimeType mimeType) {
    Objects.requireNonNull(uuid, "uuid must not be null");

    String extension = "undefined";
    if (mimeType == null) {
      mimeType = MimeType.MIME_APPLICATION_OCTET_STREAM;
    }
    if (!mimeType.getExtensions().isEmpty()) {
      extension = mimeType.getExtensions().get(0);
    }
    String primaryType = mimeType.getPrimaryType();

    final String uuidStr = uuid.toString();
    String uuidPath = getSplittedUuidPath(uuidStr);

    Path path =
        Paths.get(
            repositoryFolderPath,
            primaryType,
            extension,
            uuidPath,
            DEFAULT_FILENAME_WITHOUT_EXTENSION);
    String location = "file://" + path.toString();
    if (!extension.isBlank() && !"undefined".equals(extension)) {
      location = location + "." + extension;
    }
    // example location =
    // file:///local/cudami/resourceRepository/application/xml/a30c/f362/5992/4f5a/8de0/6193/8134/e721/resource.xml

    return URI.create(location);
  }

  @Override
  public FileResource find(String uuidStr, MimeType mimeType)
      throws TechnicalException, ResourceNotFoundException {
    FileResource resource = new FileResource();

    final UUID uuid = UUID.fromString(uuidStr);
    resource.setUuid(uuid);

    URI uri = createUri(uuid, mimeType);
    if (!resourceLoader.getResource(uri.toString()).isReadable()) {
      throw new TechnicalException("File resource at uri " + uri + " is not readable");
    }
    resource.setUri(uri);

    String filename = uri.toString().substring(uri.toString().lastIndexOf("/"));
    resource.setFilename(filename);

    resource.setMimeType(MimeType.fromFilename(filename));

    Resource springResource = resourceLoader.getResource(uri.toString());

    long lastModified = getLastModified(springResource);
    if (lastModified != 0) {
      // lastmodified by code in java.io.File#lastModified (is also used in Spring's
      // core.io.Resource) is in milliseconds!
      resource.setLastModified(
          Instant.ofEpochMilli(lastModified).atOffset(ZoneOffset.UTC).toLocalDateTime());
    } else {
      resource.setLastModified(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    }

    long length = getSize(springResource);
    if (length > -1) {
      resource.setSizeInBytes(length);
    }
    return resource;
  }

  @Override
  public byte[] getAsBytes(FileResource resource)
      throws TechnicalException, ResourceNotFoundException {
    try {
      assertReadability(resource);
      return IOUtils.toByteArray(this.getInputStream(resource));
    } catch (IOException ex) {
      String msg = "Could not read bytes from resource: " + resource;
      LOGGER.error(msg, ex);
      throw new TechnicalException(msg, ex);
    }
  }

  @Override
  public Document getAsDocument(FileResource resource)
      throws TechnicalException, ResourceNotFoundException {
    Document doc = null;
    try {
      // get InputStream on resource
      try (InputStream is = getInputStream(resource)) {
        // create Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(is);
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Got document: " + doc);
      }
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      throw new TechnicalException(
          "Cannot read document from resolved resource '" + resource.getUri().toString() + "'", ex);
    }
    return doc;
  }

  private void setImageProperties(ImageFileResource fileResource) throws IOException {
    try (ImageInputStream in = ImageIO.createImageInputStream(new File(fileResource.getUri()))) {
      final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        try {
          reader.setInput(in);
          fileResource.setWidth(reader.getWidth(0));
          fileResource.setHeight(reader.getHeight(0));
        } finally {
          reader.dispose();
        }
      }
    }
  }

  public InputStream getInputStream(URI resourceUri)
      throws TechnicalException, ResourceNotFoundException {
    try {
      String location = resourceUri.toString();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Getting inputstream for location '{}'.", location);
      }
      final Resource resource = resourceLoader.getResource(location);
      if (!resourceUri.getScheme().startsWith("http") && !resource.exists()) {
        throw new ResourceNotFoundException("Resource not found at location '" + location + "'");
      }
      return resource.getInputStream();
    } catch (IOException e) {
      throw new TechnicalException(e);
    }
  }

  @Override
  public InputStream getInputStream(FileResource resource)
      throws TechnicalException, ResourceNotFoundException {
    return getInputStream(resource.getUri());
  }

  protected long getLastModified(Resource springResource) {
    try {
      return springResource.lastModified();
    } catch (FileNotFoundException e) {
      LOGGER.warn("Resource " + springResource.toString() + " does not exist.");
    } catch (IOException ex) {
      LOGGER.warn("Can not get lastModified for resource " + springResource.toString(), ex);
    }
    return -1;
  }

  public Reader getReader(FileResource resource, Charset charset)
      throws TechnicalException, ResourceNotFoundException {
    return new InputStreamReader(this.getInputStream(resource), charset);
  }

  protected long getSize(Resource springResource) {
    try {
      long length = springResource.contentLength();
      return length;
    } catch (IOException ex) {
      LOGGER.warn("Can not get size for resource " + springResource.toString(), ex);
    }
    return -1;
  }

  protected String getSplittedUuidPath(String uuid) {
    // regex
    // '^([0-9a-f]{4})([0-9a-f]{4})-([0-9a-f]{4})-([1-5][0-9a-f]{3})-([89ab][0-9a-f]{3})-([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})$' could be used, too...
    String uuidWithoutDashes = uuid.replaceAll("-", "");
    String[] pathParts = splitEqually(uuidWithoutDashes, 4);
    String splittedUuidPath = String.join(File.separator, pathParts);
    return splittedUuidPath;
  }

  @Override
  public void save(FileResource fileResource, InputStream binaryData) throws TechnicalException {
    Assert.notNull(fileResource, "fileResource must not be null");
    Assert.notNull(binaryData, "binaryData must not be null");

    try {
      if (fileResource.isReadonly()) {
        throw new TechnicalException(
            "fileResource is read only, does not support write-operations.");
      }

      URI uri = createUri(fileResource.getUuid(), fileResource.getMimeType());
      fileResource.setUri(uri);

      //      final String scheme = uri.getScheme();
      //      if ("http".equals(scheme) || "https".equals(scheme)) {
      //        throw new TechnicalException("Scheme not supported for write-operations: " + scheme
      // + " (" + uri + ")");
      //      }
      final Path parentDirectory = Paths.get(uri).getParent();
      if (parentDirectory == null) {
        throw new TechnicalException("No parent directory defined for uri: " + uri);
      }
      Files.createDirectories(parentDirectory);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Writing: " + uri);
      }
      long size = IOUtils.copyLarge(binaryData, new FileOutputStream(Paths.get(uri).toFile()));
      fileResource.setSizeInBytes(size);

      fillAttributes(fileResource);
    } catch (IOException ex) {
      String msg = "Error writing binary data of fileresource " + fileResource.getUuid().toString();
      throw new TechnicalException(msg, ex);
    }
  }

  protected void fillAttributes(FileResource fileResource) throws IOException {
    if (fileResource instanceof ImageFileResource) {
      ImageFileResource imageFileResource = (ImageFileResource) fileResource;
      setImageProperties(imageFileResource);
      setIiifProperties(imageFileResource);
    } else if (fileResource instanceof VideoFileResource) {
      VideoFileResource videoFileResource = (VideoFileResource) fileResource;
      setVideoProperties(videoFileResource);
    }
  }

  @Override
  public void save(FileResource resource, String input, Charset charset) throws TechnicalException {
    try (InputStream in = new ReaderInputStream(new StringReader(input), charset)) {
      save(resource, in);
    } catch (IOException ex) {
      String msg = "Could not write data to uri " + String.valueOf(resource.getUri());
      LOGGER.error(msg, ex);
      throw new TechnicalException(msg, ex);
    }
  }

  /**
   * Convert "Thequickbrownfoxjumps" to String[] {"Theq","uick","brow","nfox","jump","s"}
   *
   * @param text text to split
   * @param partLength length of parts
   * @return array of text parts
   */
  private String[] splitEqually(String text, int partLength) {
    if (!StringUtils.hasText(text) || partLength == 0) {
      return new String[] {text};
    }

    int textLength = text.length();

    // Number of parts
    int numberOfParts = (textLength + partLength - 1) / partLength;
    String[] parts = new String[numberOfParts];

    // Break into parts
    int offset = 0;
    int i = 0;
    while (i < numberOfParts) {
      parts[i] = text.substring(offset, Math.min(offset + partLength, textLength));
      offset += partLength;
      i++;
    }

    return parts;
  }

  private void setIiifProperties(ImageFileResource imageFileResource) {
    if (iiifImageBaseUrl != null) {
      try {
        String iiifUrl = iiifImageBaseUrl.toString();
        if (!iiifUrl.endsWith("/")) {
          iiifUrl += "/";
        }
        iiifUrl += imageFileResource.getUuid().toString();
        imageFileResource.setHttpBaseUrl(URI.create(iiifUrl).toURL());
      } catch (MalformedURLException ex) {
        throw new IllegalStateException(
            "Creating a valid iiif url failed! Check configuration!", ex);
      }
    }
  }

  private void setVideoProperties(VideoFileResource videoFileResource) {
    if (mediaVideoBaseUrl != null) {
      try {
        String videoUrl = mediaVideoBaseUrl.toString();
        if (!videoUrl.endsWith("/")) {
          videoUrl += "/";
        }
        videoUrl += videoFileResource.getUuid().toString();
        videoFileResource.setHttpBaseUrl(URI.create(videoUrl).toURL());
      } catch (MalformedURLException ex) {
        throw new IllegalStateException(
            "Creating a valid video url failed! Check configuration!", ex);
      }
    }
  }
}
