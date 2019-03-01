# Cudami Client (Spring)

Spring based cudami client.

TODO: maybe whole business and backend layers of cudami Admin could go here and in cudami Client (OpenFeign)...

## Configuration

Add library to `pom.xml` of your project:

```xml
<dependency>
  <groupId>de.digitalcollections.cudami</groupId>
  <artifactId>dc-cudami-client-spring</artifactId>
  <version>${version.cudami-client-spring}</version>
</dependency>
```

Configure cudami server address environment (e.g. in your `application.yml`):

```yml
cudami:
  server:
    address: https://api.myserver.com/cudami
```

Configure unique id of cudami pages (e.g. in your `application.yml`):

```yml
cudami:
  server:
    address: https://api.myserver.com/cudami
  webpages:
    imprint: '91ad474d-a463-41de-a367-51b196eec621'
    privacy: '355b2353-76e4-480a-9a7a-8747e2836b36'
```

Add cudami spring beans to your application context (e.g. by importing cudami config):

Example `SpringConfig.java`:

```java
import de.digitalcollections.cudami.client.spring.config.SpringConfigCudami;

@Configuration
@Import(SpringConfigCudami.class)
public class SpringConfig {

}
```

## Usage

### Get webpage from cudami and render it

After configuring the unique id for a webpage, you can get a webpage from cudami server using the cudami spring client (configuration see above). cudami offers also a Thymeleaf rendering fragment (`cudami/fragments/webpage-to-html`) to render a webpage in a template.

Example `CudamiWebpageController.java`:

```java
package org.mdz.mdz.homepage.webapp.controller;

import de.digitalcollections.cudami.client.spring.business.CudamiException;
import de.digitalcollections.cudami.client.spring.business.CudamiService;
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
  private CudamiService cudamiService;

  @Value(value = "${cudami.webpages.imprint}")
  private UUID uuidImprint;

  @Value(value = "${cudami.webpages.privacy}")
  private UUID uuidPrivacy;

  private Webpage getWebpage(UUID uuid) {
    Webpage webpage;
    try {
      Locale locale = LocaleContextHolder.getLocale();
      webpage = cudamiService.getWebpage(locale, uuid);
    } catch (CudamiException ex) {
      // fallback to static text with link to external privacy page
      webpage = null;
    }
    return webpage;
  }

  private Locale getLocale(Webpage webpage) {
    if (webpage == null) {
      return null;
    }
    Locale returnedLocale = webpage.getLabel().getLocales().iterator().next();
    return returnedLocale;
  }

  /**
   * Imprint page.
   *
   * @param model the model
   * @return view
   */
  @RequestMapping(value = {"/imprint"}, method = RequestMethod.GET)
  public String imprint(Model model) {
    Webpage webpage = getWebpage(uuidImprint);
    Locale returnedLocale = getLocale(webpage);
    model.addAttribute("locale", returnedLocale);
    model.addAttribute("webpage", webpage);
    return "imprint";
  }

  /**
   * Privacy page.
   *
   * @param model the model
   * @return view
   */
  @RequestMapping(value = {"/privacy"}, method = RequestMethod.GET)
  public String privacy(Model model) {
    Webpage webpage = getWebpage(uuidPrivacy);
    Locale returnedLocale = getLocale(webpage);
    model.addAttribute("locale", returnedLocale);
    model.addAttribute("webpage", webpage);
    return "privacy";
  }
}
```

Example Thymeleaf template `imprint.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{base}">
  <body>
    <section layout:fragment="content">
      <section class="imprint">
        <div class="row">
          <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2">
            <div th:if="${webpage}" th:insert="cudami/fragments/webpage-to-html :: renderWebpage(${webpage}, ${locale})"></div>
            <div th:unless="${webpage}">
              <span th:utext="#{legal_fallback_imprint}">...</span>
            </div>
          </div>
        </div>
      </section>
    </section>

  </body>
</html>

```
