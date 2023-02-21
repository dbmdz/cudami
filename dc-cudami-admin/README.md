# cudami Management Webapp

This is the GUI for administrating (content and users in) cudami.

## Usage

Start webapp JAR by specifiying cudami backend rest url using params:

Example:

``` sh
java -jar dc-cudami-admin-webapp-7.0.0-SNAPSHOT.jar --cudami.server.address=<your_endpoint_address> --cudami.server.url=<your_endpoint_url>
```

## Migrations

### Migration to Thymeleaf 3

References:

* http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html
* https://ultraq.github.io/thymeleaf-layout-dialect/MigrationGuide.html
* https://github.com/thymeleaf/thymeleaf/issues/451