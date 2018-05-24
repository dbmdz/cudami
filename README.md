# cudami (cultural digital asset management)

[![Javadocs](http://javadoc.io/badge/de.digitalcollections.cudami/dc-cudami.svg)](http://javadoc.io/doc/de.digitalcollections.cudami/dc-cudami)
[![Build Status](https://travis-ci.org/dbmdz/cudami.svg?branch=master)](https://travis-ci.org/dbmdz/cudami)
[![Codebeat](https://codebeat.co/badges/f592b49d-3ba1-407b-a6b4-6e7c9850a7b0)](https://codebeat.co/projects/github-com-dbmdz-cudami-master)
[![Codecov](https://codecov.io/gh/dbmdz/cudami/branch/master/graph/badge.svg)](https://codecov.io/gh/dbmdz/cudami)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/cudami.svg?maxAge=2592000)](https://github.com/dbmdz/cudami/releases)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections.cudami/dc-cudami.svg?maxAge=2592000)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22cudami%22)

cudami is an editorial backoffice for managing cultural digital assets like websites, articles, digitized objects, digital native objects and entities.

Technologies used:

* Overall: Java, Spring Boot, Spring Security
* Frontend: Spring MVC, Thymeleaf
* Business: Java
* Backend: JDBI/PostgreSql, Flyway

Architecture:

* Cudami repository server with REST-interface
* Cudami webapp GUI connected over REST-Interface with cudami repository server as backend.

Model:

* cudami Model is in sub-module dc-cudami-model
* Modelling is done with easyUML-Netbeans-Plugin (see <http://plugins.netbeans.org/plugin/55435/easyuml>) and stored in "dc-cudami/dc-cudami-model-parent/UMLDiagrams".

Features:

* Automatic admin user wizard.
* Multilingual GUI.
* Login/logout.
* User management (CRUD)
* Session logging incl. AOP-logging
* Layer modularization (Frontend, Business, Backend; each API and IMPL)
* Completely REST-based repository

## Installation

1.  Install PostgreSql:

    on Ubuntu:

        $ apt-cache search postgresql
        ...
        postgresql - object-relational SQL database (supported version)
        postgresql-9.4 - object-relational SQL database, version 9.4 server
        ...
        $ sudo apt-get install postgresql


2.  Create a database on your PostgreSql instance:

        $ sudo su - postgres
    
        ($ dropdb 'cudami')
    
        $ psql -c "CREATE USER cudami PASSWORD 'somepassword';"
    
        CREATE ROLE
    
        $ createdb cudami -O cudami

    Check:

    List databases:

        $ psql -l
                                          List of databases
           Name    |  Owner   | Encoding |   Collate   |    Ctype    |   Access privileges   
        -----------+----------+----------+-------------+-------------+-----------------------
         postgres  | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
         template0 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
                   |          |          |             |             | postgres=CTc/postgres
         template1 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
                   |          |          |             |             | postgres=CTc/postgres
         cudami    | cudami   | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
         (4 rows)

    List tables of database cudami:

        $ psql -d cudami
        psql (9.5.7, server 9.4.8)
        Type "help" for help.

        cudami=# \d
        No relations found.
        cudami=# \q

3. Put your database properties into configuration file(s):

        $ cd <cudami source directory>
        $ vi dc-cudami-server/dc-cudami-server-backend-jdbi/src/main/resources/de/digitalcollections/cudami/config/SpringConfigBackend-<profile>.properties

        database.name=cudami
        database.hostname=localhost
        database.password=somepassword
        database.port=5432
        database.username=cudami

## Build

Build CMS:

    $ cd <cudami source directory>
    $ mvn clean install

## Usage

### Run cudami (with "local" profile = configuration for local test)

Start repository and then cudami GUI webapp:

```sh
$ java -jar dc-cudami-server-webapp-1.1.0-SNAPSHOT-exec.jar --spring.profiles.active=local &
$ java -jar dc-cudami-client-webapp-1.1.0-SNAPSHOT-exec.jar --spring.profiles.active=local &
```

### Run cudami (with "PROD" profile = configuration for production)

Start repository and then cudami GUI webapp:

```sh
$ java -jar dc-cudami-server-webapp-1.1.0-SNAPSHOT-exec.jar --spring.profiles.active=PROD &
$ java -jar dc-cudami-client-webapp-1.1.0-SNAPSHOT-exec.jar --spring.profiles.active=PROD &
```

### GUI

Local running cudami: http://localhost:9898

cudami GUI webapp connects to cudami repository server and if no admin user exists, the admin user creation assistant is launched.
Create an admin user and log in.

Enjoy!
