# cudami Management Webapp

This is the GUI for administrating (content and users in) cudami.

## Usage

Start webapp JAR by specifiying cudami backend rest url using params:

Example:

``` sh
java -jar dc-cudami-admin-webapp-7.0.0-SNAPSHOT.jar --cudami.server.address=<your_endpoint_address> --cudami.server.url=<your_endpoint_url>
```

## Frameworks

### Javascript and CSS

* AdminLTE 3.2.0
* Bootstrap-Table 1.21.1
* jQuery 3.6.0
* Bootstrap v4.6.1 (https://getbootstrap.com/)
* Font Awesome Free 5.15.4
* TipTap 2.0.0beta
* filesize 9.0.1
* jQuery-Autocomplete 1.4.11 (<https://github.com/devbridge/jQuery-Autocomplete>)

## Migrations

### Migration to Thymeleaf 3

References:

* http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html
* https://ultraq.github.io/thymeleaf-layout-dialect/MigrationGuide.html
* https://github.com/thymeleaf/thymeleaf/issues/451