package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.Website;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * see {@link Website}
 */
public class WebsiteImpl extends IdentifiableImpl implements Website {

  private Text label;
  private LocalDate registrationDate;
  private List<Node> rootNodes;
  private URL url;

  public WebsiteImpl() {
  }

  public WebsiteImpl(URL url) {
    this(null, url, null);
  }

  public WebsiteImpl(List<Node> rootNodes, URL url, LocalDate registrationDate) {
    this.registrationDate = registrationDate;
    this.rootNodes = rootNodes;
    this.url = url;
  }

  @Override
  public Text getLabel() {
    return label;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
  }

  @Override
  public LocalDate getRegistrationDate() {
    return registrationDate;
  }

  @Override
  public void setRegistrationDate(LocalDate registrationDate) {
    this.registrationDate = registrationDate;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public List<Node> getRootNodes() {
    return rootNodes;
  }

  @Override
  public void setRootNodes(List<Node> rootNodes) {
    this.rootNodes = rootNodes;
  }

}
