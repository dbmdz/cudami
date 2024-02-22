package io.github.dbmdz.cudami.model.monitoring;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "Service:type=JMX,name=VersionInfo")
public class VersionInfo implements InitializingBean {

  @Value("${info.app.project.artifactId:unknown}")
  private String projectArtifactId;

  @Value("${info.app.project.version:unknown}")
  private String projectVersion;

  @Value("${info.app.project.buildDetails:unknown}")
  private String projectBuildDetails;

  @Value("${info.app.project.name:}")
  private String projectName;

  @Autowired private AbstractEnvironment env;

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class);
  private static final Pattern JAR_PATTERN = Pattern.compile(".*/(.*?)!");
  private static final Pattern VERSION_FROM_FILENAME_PATTERN =
      Pattern.compile("(.*)-([^SR].*)\\.jar$");
  private static final String[] VERSION_KEYS = {
    "Implementation-Version", "Bundle-Version", "Version"
  };
  Map<String, String> versions = new TreeMap<>();

  @Override
  public void afterPropertiesSet() throws Exception {
    versions.put(projectArtifactId, projectBuildDetails);

    try {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream is = url.openStream()) {
          Manifest manifest = new Manifest(is);
          Attributes manifestAttributes = manifest.getMainAttributes();
          String artifactName = getArtifactNameFromUrl(url);
          String version = extractVersionFromManifest(manifestAttributes, artifactName);
          versions.put(artifactName, version);
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Cannot read manifests: " + e, e);
    }

    LOGGER.info("Version=" + projectVersion);
  }

  @ManagedAttribute
  public String getVersionInfo() {
    return projectVersion;
  }

  @ManagedAttribute
  public String getApplicationName() {
    return projectName;
  }

  @ManagedAttribute
  public String getBuildDetails() {
    return projectBuildDetails;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "managed attribute")
  @ManagedAttribute
  public Map<String, String> getArtifactVersions() {
    return versions;
  }

  private String getArtifactNameFromUrl(URL url) {
    String path = url.getPath();
    Matcher matcher = JAR_PATTERN.matcher(path);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return "unknown";
  }

  private String extractVersionFromManifest(Attributes attributes, String fallback) {
    for (String versionKey : VERSION_KEYS) {
      String version = attributes.getValue(versionKey);
      if (version != null && !version.isEmpty()) {
        return version;
      }
    }

    Matcher matcher = VERSION_FROM_FILENAME_PATTERN.matcher(fallback);
    if (matcher.find()) {
      return "~ " + matcher.group(2);
    }

    return "unknown (tried everything)";
  }
}
