# cudami (cultural digital asset management system)

[![Javadocs](https://javadoc.io/badge/de.digitalcollections.cudami/dc-cudami.svg)](https://javadoc.io/doc/de.digitalcollections.cudami/dc-cudami)
[![Build Status](https://img.shields.io/travis/dbmdz/cudami/master.svg)](https://travis-ci.org/dbmdz/cudami)
[![Codecov](https://img.shields.io/codecov/c/github/dbmdz/cudami/master.svg)](https://codecov.io/gh/dbmdz/cudami)
[![License](https://img.shields.io/github/license/dbmdz/cudami.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/cudami.svg)](https://github.com/dbmdz/cudami/releases)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections.cudami/dc-cudami.svg)](https://search.maven.org/search?q=a:dc-cudami)

cudami is an editorial backoffice for managing cultural digital assets like websites, articles, digitized objects, digital native objects and entities.

Technologies used:

* Overall: Java, Spring Boot, Spring Security
* Frontend: Spring MVC, Thymeleaf, React
* Business: Java
* Backend: JDBI/PostgreSQL, Flyway

Architecture:

* cudami repository server with REST-interface
* cudami admin webapp connected over REST-Interface with cudami repository server as backend.

Model:

* cudami model can be found [here](https://github.com/dbmdz/digitalcollections-model)
* Modelling is done with the yEd Graph Editor (see <https://www.yworks.com/products/yed>)

Features:

* Automatic admin user wizard.
* Multilingual GUI.
* Login/logout.
* User management (CRUD)
* Session logging incl. AOP-logging
* Layer modularization (Frontend, Business, Backend; each API and IMPL)
* Completely REST-based repository

## Requirements

- the minimum version of the JRE used to start the applications is `17`
- the minimum version of PostgreSQL is `12`, as cudami uses features that are not available in older versions

## Installation

1.  Install PostgreSQL:

    on Ubuntu:

    ```
    $ apt-cache search postgresql
    ...
    postgresql - object-relational SQL database (supported version)
    postgresql-12 - object-relational SQL database, version 12 server
    ...
    $ sudo apt-get install postgresql
    ```

2.  Create a database on your PostgreSQL instance:

    ```
    $ sudo su - postgres
    ($ dropdb 'cudami')
    $ psql -c "CREATE USER cudami PASSWORD 'somepassword';"
    CREATE ROLE
    $ createdb cudami -O cudami
    ```

    **Check:**

    - list databases:

    ```
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
     ```

    - list tables of database cudami:

    ```
    $ psql -d cudami
    psql (12.x (Debian 12.x))
    Type "help" for help.

    cudami=# \d
    No relations found.
    cudami=# \q
    ```

3. Put your database properties into configuration file(s):

    ```
    $ cd <cudami source directory>
    $ vi /dc-cudami-server-webapp/src/main/resources/application.yml

    spring:
      ...
      datasource:
        ...
        url: "jdbc:postgresql://localhost:5432/cudami"
        username: cudami
        password: somepassword
        ...
    ```

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

### Run cudami (with "local" profile = configuration for local test)

```sh
$ java -jar dc-cudami-server/dc-cudami-server-webapp/target/dc-cudami-server-webapp-<VERSION>.jar &
$ java -jar dc-cudami-admin-webapp/target/dc-cudami-admin-webapp-<VERSION>.jar &
```

### Run cudami (with "PROD" profile = configuration for production)

```sh
$ java -jar dc-cudami-server/dc-cudami-server-webapp/target/dc-cudami-server-webapp-<VERSION>.jar --spring.profiles.active=PROD &
$ java -jar dc-cudami-admin-webapp/target/dc-cudami-admin-webapp-<VERSION>.jar --spring.profiles.active=PROD &
```

### GUI

Local running cudami: http://localhost:9898

The cudami admin webapp connects to cudami repository server and if no admin user exists, the admin user creation assistant is launched.
Create an admin user and log in.

Enjoy!

## Development Quickstart using Docker Compose

### Installation

Install Docker according to the official [Docker documentation](https://docs.docker.com/install/).
Install Docker Compose according to the official [documentation](https://docs.docker.com/compose/install/).

#### Debian 9

```sh
$ su -
# apt-get install apt-transport-https dirmngr
# echo 'deb https://apt.dockerproject.org/repo debian-stretch main' >> /etc/apt/sources.list
# apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys F76221572C52609D
Executing: /tmp/apt-key-gpghome.wDKSqs4VYM/gpg.1.sh --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys F76221572C52609D
gpg: key F76221572C52609D: public key "Docker Release Tool (releasedocker) <docker@docker.com>" imported
gpg: Total number processed: 1
gpg:               imported: 1
# apt-get update
# apt-get install docker-engine
# apt-get install docker-compose
```

### Configuration

#### All Linux

Add your user to docker group to run docker without sudo:

```shell
$ sudo groupadd docker
$ sudo gpasswd -a yourusername docker
$ sudo service docker restart
```

### Usage

To get cudami quickly up running, you can start all backend services using Docker Compose.
If you want to start your Postgres database and load a dump or backup file then you can copy or link any
supported dump file into `docker/repository/postgres-initdb.d/`. Supported file suffixes are `.dump` for binary dumps
and `.sql` for plain SQL dumps. Both formats can be zipped by 7zip (`.7z`), gzip (`.gz`) or bzip2 (`.bz2`),
e.g. `mydatabase.dump.7z` or `backup.sql.bz2`.

To build and start docker just run:

```shell
$ cd docker/repository
$ docker-compose build
$ docker-compose up -d
```

Then PostgreSQL is running in a container and everything is ready for running a local instance of cudami (see below).

To start cudami server webapp, you have to run:

```shell
$ java -jar dc-cudami-server/dc-cudami-server-webapp/target/dc-cudami-server-webapp-<VERSION>.jar &
```

The cudami server webapp is now running under <http://localhost:9000/>.

To start cudami admin webapp, you have to run:

```shell
$ java -jar dc-cudami-admin-webapp/target/dc-cudami-admin-webapp-<VERSION>.jar &
```

The cudami admin webapp is now running under <http://localhost:9898/>.

To start the react components, you have to run:

```
$ cd dc-cudami-editor/
$ npm install
$ npm start
```

To stop the container run

```shell
$ cd docker/repository
$ docker-compose stop
```

To delete the container and all data:

```shell
$ cd docker/repository
$ docker-compose down
```

### Migrations from 3.1.1 to 3.2.2

- Stop 3.1.1 cudami backend server webapp, manually rename table "schema_version" to "flyway_schema_history":

```sql
alter table schema_version rename to flyway_schema_history;
```

- Deploy cudami 3.2.2
- Start 3.2.2

### Application Configuration

The application is configured by an `application.yml`. You can either modify this file directly, or
you can use another custom `application.yml` in another directory and/or with different name, whose
location must be set as parameter `spring.config.additional-location` on startup time,
e.g. `-spring.config.additional-location=file:/your/installation/application-custom.yml`

Besides the familiar spring configuration parameters, cudami offers a number of proprietary parameters:

- `cudami.defaults.language`: Default language for labels and UrlAliases when no limitation to a specific language was
  requested.
- `cudami.defaults.locale`: Similar as above, but takes the country into account, too.
- `cudami.repositoryFolderPath`: Base directory, below which all uploaded file contents
  are stored in a hierarchy, defined by the corresponding MIME types
_ `cudami.urlalise.generationExcludes`: List of all content types, for which no UrlAliases should
  be auto-generated, e.g. `[DIGITAL_OBJECT]`.  Possible values are `AGENT, ARTICLE, AUDIO, BOOK,
  COLLECTION, CORPORATE_BODY, DIGITAL_OBJECT, ENTITY, EVENT, EXPRESSION, FAMILY, GEOLOCATION, IMAGE,
  ITEM, MANIFESTATION, OBJECT_3D, PERSON, PLACE, PROJECT, TOPIC, VIDEO, WEBSITE, WORK`
