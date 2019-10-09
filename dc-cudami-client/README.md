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

Configure cudami server url by environment (e.g. in your `application.yml`):

```yml
cudami:
  server:
    url: https://api.myserver.com/cudami
```

Configure unique id of cudami pages (e.g. in your `application.yml`):

```yml
cudami:
  server:
    url: https://api.myserver.com/cudami
  webpages:
    foobar: '91ad474d-a463-41de-a367-51b196eec621'
```

Add cudami spring beans to your application context:

Example `SpringConfig.java`:

```java
import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.CudamiClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
  ...
  @Bean
  public CudamiClient cudamiClient() {
    return CudamiClient.build(serverUrl);
  }
  ...
}
```

## Usage

### Get webpage from cudami and render it

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

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiWebpageController.class);

  @Autowired
  private CudamiClient cudamiClient;

  @Value(value = "${cudami.webpages.foobar}")
  private UUID uuid;

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
      webpage = cudamiClient.getWebpage(locale, uuid.toString());
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
