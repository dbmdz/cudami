# cudami Management Webapp

This is the GUI for administrating (content and users in) cudami.

## Requirements

The minimum version of the JRE used to start the applications is `17`.

## Build

### Development

```
$ cd <cudami source directory>
$ mvn clean install
```

### Production

```
$ cd <cudami source directory>
$ mvn clean install -Pproduction
```

## Usage

Start webapp JAR by specifiying cudami backend rest url using params:

Example:

``` sh
java -jar dc-cudami-admin-webapp-7.0.0-SNAPSHOT.jar --cudami.server.address=<your_endpoint_address> --cudami.server.url=<your_endpoint_url>
```

### GUI

Local running cudami: http://localhost:9898

The cudami admin webapp connects to cudami repository server and if no admin user exists, the admin user creation assistant is launched.
Create an admin user and log in.

Enjoy!

## Frameworks

### Javascript and CSS

* AdminLTE 3.2.0
* Bootstrap v4.6.1 (https://getbootstrap.com/)
* Bootstrap-Table 1.21.2
* filesize 9.0.1
* Font Awesome Free 5.15.4
* jQuery 3.6.0
* jQuery-Autocomplete tomik23/autocomplete 1.8.6
* TableDnD 1.0.4 (http://isocra.github.io/TableDnD/)
* TipTap 2.0.0beta

## Migrations

### Migration to Thymeleaf 3

References:

* http://www.thymeleaf.org/doc/articles/thymeleaf3migration.html
* https://ultraq.github.io/thymeleaf-layout-dialect/MigrationGuide.html
* https://github.com/thymeleaf/thymeleaf/issues/451
