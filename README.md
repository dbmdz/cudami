# cudami (cultural digital asset management)

cudami is an editorial backoffice for managing cultural digital assets like websites, articles, digitized objects, digital native objects and entities.

Technologies used:

* Overall: Java, Spring Boot, Spring Security
* Frontend: Spring MVC, Thymeleaf
* Business: Java
* Backend: JDBI/PostgreSql, Flyway

Architecture:

* Cudami repository server with REST-interface
* Cudami webapp GUI connected over REST-Interface with cudami repository server as backend.

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

CMS connects to database and if no admin user exists, the admin user creation assistant is launched.
Create an admin user and log in.

Enjoy!