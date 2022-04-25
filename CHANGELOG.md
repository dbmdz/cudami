# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Handling of `LinkedDataFileResources`
- Added missing columns for unique objects
- Added sorting of resources in `dc-cudami-admin-webapp`

### Changed

- Using `digitalcollections-model` in version `9.2.0`
- Database update for the updated `DigitalObject`
- Fill new fields of `DigitalObject`, when a single `DigitalObject` is returned
- Improved `Identifier` handling by avoiding useless deletions and re-insertions
- Test updates
- Fixed handling of IdentifierTypes
- Fixed filtering of `LocalizedUrlAliases` by `Locale`
- Added collation to `varchar` columns
- Changed default sorting of identifiables in `dc-cudami-server-webapp` from `label` in default language to `lastModified` and `uuid`
- Updated multiple dependencies
- Normalize method names: `getBy` for returning one object, `findBy` for returning a list of objects, "set" for saving a list after deleting previous list

### Removed

Methods returning one object renamed from `find...` to `getBy...`:

- `findOne(UUID uuid)`-methods, use `getByUuid`-method instead
- `findOne(UUID uuid, Locale locale)`-methods, use `getByUuidAndLocale`-method instead
- `findOneByIdentifier(String namespace, String id)`-methods, use `getByIdentifier`-method instead
- `findOneByRefId(long refId)`-methods, use `getByRefId`-method instead
- `findOneByEmail(String email)`-methods, use `getByEmail`-method instead
- `findOneByLabelAndLocale`-methods, use `getByLabelAndLocale`-method instead
- `findOne(UUID uuid, Filtering filtering)`-methods, use `getByUuidAndFiltering`-method instead
- `findOneByNamespace(String namespace)`-methods, use `getByNamespace`-method instead

## [5.2.3](https://github.com/dbmdz/cudami/releases/tag/5.2.3) - 2022-04-01

### Changed

- Updated Spring Boot to fix CVE-2022-22965

## [5.2.2](https://github.com/dbmdz/cudami/releases/tag/5.2.2) - 2022-03-15

### Changed

- Fixed update of own password

## [5.2.1](https://github.com/dbmdz/cudami/releases/tag/5.2.1) - 2022-02-08

### Added

- Added indexes on big tables to improve performance of joins and sorting

### Changed

- Fixed search for a website's rootpages
- Fixed two wrong endpoints endpoints called in the client
- Improved paging: added unique field to sorting

## [5.2.0](https://github.com/dbmdz/cudami/releases/tag/5.2.0) - 2022-02-07

### Added

- Added management of UrlAliases
- Added and pre-filled table `url_aliases` via Flyway migration
- Improved some parts of the admin GUI
- Bumped `dc-model` dependency to version `9.1.0`
- Added search for `url` on websites
- Added transactions to all SQL statements by annotation on class level in business service layer
- Added config endpoint, mainly for admin GUI
- Added HeadwordEntry and Headword endpoints for management of dictionaries / encyclopedias
- Added `uuid` to allowed sorting fields
- Added management of Licenses

### Changed

- Refactored relation between business service and repository layers
- Refactored most of the class components to function components in [dc-cudami-editor](dc-cudami-editor/)
- Fixed search in nested JSONB fields

### Removed

- Removed foreign keys that do not work with PostgreSQL table inheritance

## [5.1.0](https://github.com/dbmdz/cudami/releases/tag/5.1.0) - 2021-08-17

### Added

- Added better error handling with display of an error message
- Added codevov (https://about.codecov.io/) to the CI
- Added functionality to add an attached identifiable by `refId`
- Added display of `refId` to view page of collections
- Added search functionality to some more identifiable lists

### Changed

- Improved datepicker design
- Optimised ci caching
- Optimised display of empty language
- Fixed alt text of preview image
- Refactored form for users to react

## [5.0.1](https://github.com/dbmdz/cudami/releases/tag/5.0.1) - 2021-07-26

### Changed

- Bumped `dc-model` dependency to version `9.0.0`

## [5.0.0](https://github.com/dbmdz/cudami/releases/tag/5.0.0) - 2021-07-06

### Added

- Added filtering for entity-type(s) when loading lists of `Entity`
- Added template for table of content rendering
- Implemented list of persons and geolocations
- Added sorting of imports in `dc-cudami-editor`
- Added possibility to define mailto links in the editor
- Added search functionality to many identifiable lists
- Added many controller and repository tests
- Added functionality to remove languages from content
- Added `v5` endpoints
- Added request id header (if present) to logging
- Added possibility to build releases on the CI

### Changed

- Migrated to `dc-model` in version `9`
- Restructured packages in `dc-cudami-client` to be synchronous with other modules
- Migrated list of articles, attached entities/files corporate bodies, digital objects, file resources, identifier types, projects, (sub)topics, users to react
- Refactored structure and names of react components
- Fixed deletion for empty identifier lists
- Replaced old sorting syntax with new one
- Refactored webpack configuration
- Refactored design to `AdminLTE` (https://adminlte.io/)
- Refactored form for identifier types to react
- Fixed session configuration
- Replaced jsondoc with openapi/swagger
- Refactored to `react` in version 17
- Added usage of `HTTP/1.1` by default in `dc-cudami-client`

### Removed

- Removed `EntityPart` and `Subtopic`
- Removed XML stuff
- Removed development application in `dc-cudami-editor`

## [4.5.3](https://github.com/dbmdz/cudami/releases/tag/4.5.3) - 2021-06-22

### Changed

- Added usage of `HTTP/1.1` by default in `dc-cudami-client`

## [4.5.2](https://github.com/dbmdz/cudami/releases/tag/4.5.2) - 2021-06-07

### Changed

- Fixed an error that happens when a `searchTerm` is inserted into a jsonpath `like_regex` search that contains double quotes (#1045)
- Fixed security restriction for `/users/updatePassword`
- Fixed check if label should be rendered

### Removed

- Removed checkbox to activate `remember-me` functionality, as it is not working at the moment

## [4.5.1](https://github.com/dbmdz/cudami/releases/tag/4.5.1) - 2021-05-17

### Changed

- Fixed sticky button in the navbar

## [4.5.0](https://github.com/dbmdz/cudami/releases/tag/4.5.0) - 2021-04-08

### Changed

- Migrated all API calls to `latest` endpoints in `dc-cudami-client` to use the versioned endpoints

## [4.4.4](https://github.com/dbmdz/cudami/releases/tag/4.4.4) - 2021-04-01

### Changed

- Fixed term search in description and label

## [4.4.3](https://github.com/dbmdz/cudami/releases/tag/4.4.3) - 2021-03-25

### Changed

- Fixed search for identifiables

## [4.4.2](https://github.com/dbmdz/cudami/releases/tag/4.4.2) - 2021-03-19

### Added

- Added possibility to define mailto links

### Changed

- Fixed removal of digital objects, which contain no identifiers 

## [4.4.1](https://github.com/dbmdz/cudami/releases/tag/4.4.1) - 2021-02-24

### Changed

- Fixed sorting
- Fixed getting of creators of article
- Fixed return of preview image and fileresource type
- Fixed retrieval of fileresource with identifier

## [4.4.0](https://github.com/dbmdz/cudami/releases/tag/4.4.0) - 2021-02-08

### Added

- Added functionality to define external http hooks for save and update actions
- Introduced random lists and person family- and givennames

### Changed

- Refactored the whole SQL backend
- Fixed editing of other users
- Refactored to new sorting param syntax 

## [4.3.0](https://github.com/dbmdz/cudami/releases/tag/4.3.0) - 2021-01-22

### Added

- Made the edit and save buttons sticky
- Added endpoints for retrieval of websites' and top collections' languages

### Changed

- Refactored list of top collections and websites to react
- Fixed the query for a digital objects' active collections

## [4.2.0](https://github.com/dbmdz/cudami/releases/tag/4.2.0) - 2021-01-11

### Added

- Added a new endpoint to retrieve all related corporate bodies to a collection
- Added publication date and creators to article
- Added possibility to enter the url of a corporate body
- Added endpoints to change the order of (sub) webpages
- Added support for sorting by multiple fields and subfields
- Added functionality for adding a rendering template to a webpage
- Added a switch to the webpage form to activate/deactivate the in-page navigation
- Added person birth and death locations, geo locations and humansettlements

### Removed

- Removed the field `description` from the search for collections and digital objects

## [4.1.1](https://github.com/dbmdz/cudami/releases/tag/4.1.1) - 2020-11-10

### Changed

- Add pattern for url validation to also allow relative urls

## [4.1.0](https://github.com/dbmdz/cudami/releases/tag/4.1.0) - 2020-11-03

### Added

- Added paged retrieval of a website's rootpages

### Changed

- Refactored list of webpages to React

## [4.0.0](https://github.com/dbmdz/cudami/releases/tag/4.0.0) - 2020-10-27

### Added

- Added custom attributes to entity
- Added filtering possibilities for active webpages
- Added functionality to retrieve a children tree for a webpage
- Added functionality to get relations by predicate

### Changed

- Renamed "corporation" to "corporate body"

### Removed

## [3.7.0](https://github.com/dbmdz/cudami/releases/tag/3.7.0) - 2020-10-05

### Added

- Added an endpoint that returns a reduced digitalobjects list
- Added handling of items and works
- Added functionality to edit links
- Added GND fetch and save endpoint
- Added get by identifier and refid
- Made form fields optional
- Added handling of entity relations
- Added functionality to insert videos
- Introduced GitHub Actions
- Added functionality to delete a digital object with its dependencies
- Added spinner when autocomplete is loading
- Added publication dates for collections
- Added parent collections and publication status to collection view

### Changed

- Fixed the removal of iframe, image and link attributes
- Added unique constraint to identifiers

### Removed

## [3.6.0](https://github.com/dbmdz/cudami/releases/tag/3.6.0) - 2020-09-09

### Added

- Added functionality to stay in the same language when switching from the view to the edit page
- Added functionality to add and remove digital objects from a project
- Added the possibility to add, move and remove subcollections
- Added display of identifiers to identifiable list
- Added floating labels instead of placeholders for some input fields, so that the label is still visible when the is content in the field
- Added language tabs for paged list of identifiables

### Changed

- Fixed external links to open in a new tab
- Fixed link urls containing whitespace
- Made the whole application use the complete width of the screen
- Fixed the filename of images added as url to only contain the last part of the url

## [3.5.0](https://github.com/dbmdz/cudami/releases/tag/3.5.0) - 2020-08-18

### Added

- Added a view for digital objects
- Added item numbers to all lists for better referenceability and sorting impression
- Added functionality to add and remove digital objects from a collection
- Added functionality to remove digital objects from a project
- Added a legend for the publication status

### Changed

- Moved admin rest to cudami client and remove admin webapp layers/modules
- Replaced `feign` with Java `HttpClient`
- Made alt text input not required anymore
- Fixed sorting fields

## [3.4.1](https://github.com/dbmdz/cudami/releases/tag/3.4.1) - 2020-07-08

### Added

- Added a title for the button to add a preview image
- Added a notification when removing the preview image
- Added mocking of api calls for an easier development of the react parts

## [3.4.0](https://github.com/dbmdz/cudami/releases/tag/3.4.0) - 2020-07-01

### Added

- Added image preview for uploaded and selected images
- Added providing the breadcrumb via REST
- Added functionality to edit iframe and image content blocks
- Added functionality to add images without alignment/text wrapping
- Added functionality to create subcollections
- Added a dialog to add and edit a preview image

### Changed

- Fixed issues with changing the password

## [3.3.0](https://github.com/dbmdz/cudami/releases/tag/3.3.0) - 2020-05-29

### Added

- Added possibility to set start/end date of publication
- Added breadcrumbs for webpages and websites
- Added fileresource default preview image
- Added configuration properties for webjar versions
- Added the possibility to define the `title` attribute for iframes
- Added the possibility to add images in the editor
- Added a restriction for the editor, which is used for the short description: only the marks (bold, italic, etc.) can be applied
- Added the possibility to change the own password
- Added rendering of horizontal rule

### Changed

- Renamed `ContentTree` to `Topic` and `ContentNode` to `Subtopic`
- Fixed language tab sorting (by prioritised languages, if defined, and alphabetically)

### Removed

- Removed the floating toolbar
- Removed the abort button from forms

## [3.2.2](https://github.com/dbmdz/cudami/releases/tag/3.2.2) - 2019-11-25

### Added

- Added param for disabling rendering of webpage label in HTML output
- Added rendering of horizontal rule
- Added styling for blockquotes
- Added possibility for the user for updating his/her own password

### Changed

- Fixed possible NPE if fileresource is null
- Fixed conversion of multiple languages in localized structured content for xml in v1
- Refactored v1 controller to return `de_DE` instead of `de`
- Made database migrations more flexible by setting out of order migrations to `true`
- Reformated SQL to make it more readable
- Fixed rendering of html attributes
- Fixed rendering of the mark types `strikethrough`, `subscript`, `superscript` and `underline`

### Removed

- Disabled insert of footnotes and images for the moment

## [3.2.1](https://github.com/dbmdz/cudami/releases/tag/3.2.1) - 2019-11-13

### Changed

- Fixed the sorting of the language tabs
- Fixed language switch

### Removed

- Removed placeholder from editor when content is empty

## [3.2.0](https://github.com/dbmdz/cudami/releases/tag/3.2.0) - 2019-10-28

### Added

- Added digital object management (digital objects and contained file resources)
- Added entity to entities relations
- Added entity to file resources relations
- Added `CHANGELOG.md`
- Added spotbugs code checks
- Added documentation how to configure and use client
- Added deploy stage to ci configuration
- Added digital objects
- Refactored docker-compose setup to include a iiif server
- Added identifier types management
- Added `LinkedDataFileResource`
- Added auto-formatting via google style
- Added prometheus and some more monitoring endpoints
- Added management of `Collection`, `Corporation` and `Project`

### Changed

- Migrated from Bootstrap 3 to Bootstrap 4
- Migrated database schema and SQL from multiple joins to PostgreSql inheritance (where applicable)
- Bumped versions of various dependencies
- Fixed batch execution to avoid already closed connection
- Fixed link rendering (missing blank after opening a tag)
- Refactored from deprecated jdbi `findOnly` to `findOne`
- Refactored `label`, `description` and `text` of identifiables to an easier data structure
- Refactored locale to language
- Refactored the editor to `React`
- Added simplified version of client
- Added webjar versions from `pom`
- Refactored logging configuration to log to `STDOUT`
- Fixed `v2` endpoint for retrieving webpages and add `v3`

### Removed

- Removed `OracleJDK` from build matrix

## [3.1.1](https://github.com/dbmdz/cudami/releases/tag/3.1.1) - 2019-01-23

### Changed

- Fixed webpage endpoints for `v1` of the model

## [3.1.0](https://github.com/dbmdz/cudami/releases/tag/3.1.0) - 2019-01-23

### Added

- Added support for multi-language contents
- Added logic for locale specific content retrieval
- Introduced docker compose setup for local development
- Added hierarchical creation of webpages
- Added creation of content nodes
- Added automatic snapshot deployments to sonatype nexus
- Introduced articles
- Added new objects to main page and new section resources
- Introduced adding of identifiables to `Article`, `ContentNode` and `Webpage`
- Added file upload functionality
- Added `openjdk11` to build matrix
- Added endpoint versioning
- Added new template fragments for rendering of identifiables

### Changed

- Refactored to use new `dc-model`
- Fixed query for retrieving the sort index
- Fixed webapp and actuator security configuration for the admin webapp
- Fixed rendering of marks
- Bumped versions of various dependencies
- Fixed badges

### Removed

- Removed no longer used model module
