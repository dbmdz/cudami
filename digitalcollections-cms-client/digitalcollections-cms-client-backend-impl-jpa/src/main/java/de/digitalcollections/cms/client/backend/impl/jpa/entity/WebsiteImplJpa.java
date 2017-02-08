package de.digitalcollections.cms.client.backend.impl.jpa.entity;

import de.digitalcollections.cms.model.api.entity.Website;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Website entity.
 */
@Entity
@Table(name = "websites")
@Access(AccessType.FIELD)
public class WebsiteImplJpa extends EntityImplJpa implements Website<Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteImplJpa.class);

  @Id
  @TableGenerator(
          name = SequenceConstants.GENERATOR_NAME, table = SequenceConstants.TABLE_NAME,
          pkColumnName = SequenceConstants.PK_COLUMN_NAME, valueColumnName = SequenceConstants.VALUE_COLUMN_NAME,
          allocationSize = SequenceConstants.ALLOCATION_SIZE,
          pkColumnValue = "WEBSITE_SEQ")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = SequenceConstants.GENERATOR_NAME)
  @Column(name = "id")
  private Long id;

  @NotEmpty
  @Column(name = "url", nullable = false, unique = true)
  private String url;

  @NotEmpty
  @Column(name = "title", nullable = false, unique = false)
  private String title;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public URL getUrl() {
    if (url == null) {
      return null;
    }
    try {
      return URI.create(url).toURL();
    } catch (MalformedURLException ex) {
      LOGGER.error("Malformed url '" + url + "'", ex);
    }
    return null;
  }

  @Override
  public void setUrl(URL url) {
    this.url = url.toString();
  }
}
