# Custom Theme Development

For a custom theme you have to create a jar- or zip-file containing all static and thymeleaf template files of your theme.

Follow these steps for creating an own theme named e.g. `my-theme`:

1. Create a basic theme directory structure

Create a directory structure for your theme of the following structure:

```
my-theme
 |-- static
   |-- my-theme
 |-- templates
   |-- my-theme
```

2. Copy static files

Now copy all your theme's static files into the `static/my-theme` subfolder, e.g.

```
my-theme/static/my-theme/fonts/poppins-v15-latin-ext_latin_devanagari-regular.woff
my-theme/static/my-theme/fonts/poppins-v15-latin-ext_latin_devanagari-regular.svg
my-theme/static/my-theme/fonts/poppins-v15-latin-ext_latin_devanagari-regular.eot
my-theme/static/my-theme/fonts/poppins-v15-latin-ext_latin_devanagari-regular.ttf
my-theme/static/my-theme/fonts/poppins-v15-latin-ext_latin_devanagari-regular.woff2
my-theme/static/my-theme/js/main.js
my-theme/static/my-theme/js/popper.js
my-theme/static/my-theme/js/bootstrap-4.3.1.min.js
my-theme/static/my-theme/js/jquery-3.3.1.min.js
my-theme/static/my-theme/css/font-awesome-5.15.4-all.min.css
my-theme/static/my-theme/css/main.css
my-theme/static/my-theme/css/fonts.css
my-theme/static/my-theme/css/style.css 
my-theme/static/my-theme/images/...
```

3. Create Thymeleaf templates

Create at minimum two thymeleaf templates called `index.html` and `webpage.html` in the `templates/my-theme` subfolder:

```
my-theme/templates/my-theme/index.html
my-theme/templates/my-theme/webpage.html
```

Have a look at the default templates in this project under `src/main/resources/templates/colorlib-sidebar-v2`.

As you will notice these templates use a common `base.html` template for the pages basic design, just filling the content part (`fragment`) into the `base` template using the Thymeleaf Layout Dialect-Library.  For documentation on this visit the homepage of the Thymeleaf extension `[Thymeleaf Layout Dialect](https://ultraq.github.io/thymeleaf-layout-dialect/)`. The extension is included and ready to use for your themes. Using a `base.html` is strongly encouraged.

In this case at least these three templates have to be implemented:

```
my-theme/templates/my-theme/base.html
my-theme/templates/my-theme/index.html
my-theme/templates/my-theme/webpage.html
```

These model-objects are available for usage in your templates:

| Model object | Data type | Description | Scope |
| --- | --- | --- | --- |
| ${website} | [de.digitalcollections.model.identifiable.entity.Website](https://github.com/dbmdz/digitalcollections-model/blob/main/dc-model/src/main/java/de/digitalcollections/model/identifiable/entity/Website.java) | The given website to be shown | global |
| ${contentWebpages} | List&lt;[de.digitalcollections.model.identifiable.web.Webpage](https://github.com/dbmdz/digitalcollections-model/blob/main/dc-model/src/main/java/de/digitalcollections/model/identifiable/web/Webpage.java)&gt; | <ul><li>the top level webpages of the given website (if no content root page has been given by using `-Dcudami.webpages.content=...`)</li><li>the children webpages of the configured "content"-webpage (if content root page has been given)</li></ul> | global |
| ${footerWebpages} | List&lt;[de.digitalcollections.model.identifiable.web.Webpage](https://github.com/dbmdz/digitalcollections-model/blob/main/dc-model/src/main/java/de/digitalcollections/model/identifiable/web/Webpage.java)&gt; | the children webpages of the configured "footer"-webpage of the given website (if footer root page has been given by using `-Dcudami.webpages.footer=...`) | global |
| ${navMaxLevel} | int | the maximum hierarchy level of content pages to be rendered e.g. in a navigation tree (defaults to "3" if not configured by using `-Dtemplate.navMaxLevel=...`) | global |
| ${webpage} | [de.digitalcollections.model.identifiable.web.Webpage](https://github.com/dbmdz/digitalcollections-model/blob/main/dc-model/src/main/java/de/digitalcollections/model/identifiable/web/Webpage.java) | the webpage to be shown | webpage.html |

These objects are added through the controller advice class [GlobalControllerAdvice.java](src/main/java/de/digitalcollections/cudami/frontend/website/controller/advice/GlobalControllerAdvice.java) or template specific in [MainController.java](src/main/java/de/digitalcollections/cudami/frontend/website/controller/MainController.java).

4. Create theme package

Finally package the two subfolders `static` and `templates` into a JAR-file.

Example:

```
$ cd my-theme
$ jar -cvf my-theme.jar static/ templates/
```

Check archive:

```
$ jar -tvf my-theme.jar
```

5. Use theme package

* Create a `themes` directory in the directory containing the executable JAR-file of the webapp, e.g. `cudami-frontend-website-1.0.0-SNAPSHOT.jar`.
* Copy your theme's JAR-file (e.g. `my-theme.jar`) into the `themes` directory.

In order to use external JAR-files in the cudami frontend webapp, the webapp has to be started differently using the features of the spring boot [`PropertiesLauncher`](https://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html#executable-jar.property-launcher)

We have to

* give all config properties prefixed with `-D` instead of `--` (IMPORTANT: directly after `java` command)
* configure `loader.path` pointing to `themes` directory and
* specify the theme name by passing `template.name` and
* put the executable jar into classpath using `-cp` option.

This is an example command line to startup the website (replace template name with your template name):

```
$ java \
  -Dcudami.server.url=http://localhost:9000 \
  -Dcudami.website=ea9ddc66-e822-4867-9585-a43c6ed8bd98 \
  -Dcudami.webpages.content=ead664b6-5fcc-414e-b3bb-133f0af1acb8 \
  -Dcudami.webpages.footer=6bcce154-e216-4223-a4f7-d9aa99d42695 \
  -Dtemplate.name=my-theme \
  -Dtemplate.navMaxLevel=2 \
  -Dloader.path=./themes \
  -cp cudami-frontend-website-1.0.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher
```

Go and have a look at <http://localhost:8080>.

Happy theming!

