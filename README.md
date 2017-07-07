# cudami (cultural digital asset management)

cudami is an editorial backoffice for managing cultural digital assets like websites, articles, digitized objects, digital nativ objects and entities.

Technologies used:

* Overall: Java, Spring, Spring Security
* Frontend: Spring MVC, Thymeleaf
* Business: Java
* Backend: JDBI/PostgreSql, Flyway

Features:

* Automatic admin user wizard.
* Multilingual GUI.
* Login/logout.
* User management (CRUD)
* Session logging incl. AOP-logging
* Layer modularization (Frontend, Business, Backend; each API and IMPL)

* Maven Site

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
        $ vi server/server-backend-impl-jdbi/src/main/resources/de/digitalcollections/cudami/config/SpringConfigBackend-<profile>.properties

        database.name=cudami
        database.hostname=localhost
        database.password=somepassword
        database.port=5432
        database.username=cudami

## Build

Build CMS:

    $ cd <dicoCMS source directory>
    $ mvn clean install

## Usage

Run CMS (in development)
 
    $ cd <dicoCMS source directory>/digitalcollections-cms-client/digitalcollections-cms-client-webapp
    $ mvn jetty:run

Run CMS (in production)

* Deploy WAR to Tomcat
* Start with java environment variable "spring.profiles.active" set to "PROD" (-Dspring.profiles.active:PROD)

Use CMS

    Browser: http://localhost:9898

CMS connects to database and if no admin user exists, the admin user creation assistant is launched.
Create an admin user.