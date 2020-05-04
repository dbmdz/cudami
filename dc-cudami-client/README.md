# Cudami Client

OpenFeign based cudami client.

## Configuration

Add library to `pom.xml` of your project:

```xml
<dependency>
  <groupId>de.digitalcollections.cudami</groupId>
  <artifactId>dc-cudami-client</artifactId>
  <version>${version.cudami-client}</version>
</dependency>
```

### Plain Java environment (no Spring)

Create an instance of CudamiClient:

```
String serverUrl = "http://localhost:9000"; // example url of cudami server
CudamiCient cudamiClient = new CudamiClient(serverUrl);
```

### Spring Boot environment

Configure cudami server url by environment (e.g. in your `application.yml`):

```yml
cudami:
  server:
    url: https://api.myserver.com/cudami
```

Add cudami spring beans to your application context:

Example `SpringConfig.java`:

```java
import de.digitalcollections.cudami.client.CudamiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
  ...
  @Value("${cudami.server.url}")
  String serverUrl;

  @Bean
  public CudamiClient cudamiClient() {
    return new CudamiClient(serverUrl);
  }
  ...
}
```

## Usage

Your instance of CudamiClient is the single entry point to all cudami clients:

- For access to `Collection`-endpoint: `CudamiCollectionsClient cudamiCollectionsClient = cudamiClient.forCollections();`
- For access to `Corporation`-endpoint: `CudamiCorporationsClient cudamiCorporationsClient = cudamiClient.forCorporations();`
- For access to `Project`-endpoint: `CudamiProjectsClient cudamiProjectsClient = cudamiClient.forProjects();`
- For access to cudami system endpoint: `CudamiSystemClient cudamiSystemClient = cudamiClient.forSystem();`
- For access to `Webpage`-endpoint: `CudamiWebpagesClient cudamiWebpagesClient = cudamiClient.forWebpages();`

### CudamiWebpagesClient

#### Get webpage from cudami and render it

After configuring the unique id for a webpage, you can get a webpage from cudami server using the cudami client (configuration see above). cudami offers also a Thymeleaf rendering fragment (`cudami/fragments/webpage-to-html`) to render a webpage in a template.

Example `CudamiWebpageController.java`:

```java
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CudamiWebpageController {

  private final CudamiWebpagesClient cudamiWebpagesClient;
  private final UUID uuid;

  public CudamiWebpageController(@Autowired CudamiClient cudamiClient, @Value("${cudami.webpages.foobar}") UUID uuid
  ) {
    this.cudamiWebpagesClient = cudamiClient.forWebPages();
    this.uuid = uuid;
  }

  private Locale getLanguage(Webpage webpage) {
    if (webpage == null) {
      return null;
    }
    Locale returnedLocale = webpage.getLabel().getLocales().iterator().next();
    return returnedLocale;
  }

  private Webpage getWebpage(UUID uuid) {
    Webpage webpage;
    try {
      Locale locale = LocaleContextHolder.getLocale();
      webpage = cudamiWebpagesClient.getWebpage(locale, uuid.toString());
    } catch (HttpException ex) {
      // fallback to static text with link to external privacy page
      webpage = null;
    }
    return webpage;
  }

  /**
   * Cudami page.
   *
   * @param model the model
   * @return view
   */
  @RequestMapping(value = {"/foobar"}, method = RequestMethod.GET)
  public String imprint(Model model) {
    Webpage webpage = getWebpage(uuid);
    Locale returnedLocale = getLanguage(webpage);
    model.addAttribute("locale", returnedLocale);
    model.addAttribute("webpage", webpage);
    return "foobar";
  }
}
```

Example Thymeleaf template `foo.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{base}">
  <body>
    <section layout:fragment="content">
      <div th:if="${webpage}" th:insert="cudami/fragments/webpage-to-html :: renderWebpage(${webpage}, ${locale})"></div>
      <div th:unless="${webpage}">
        <span>Foobar</span>
      </div>
    </section>

  </body>
</html>

```
