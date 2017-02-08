# DigitalCollections CMS

The DigitalCollections CMS is a CMS for creating websites focussing on presenting digital objects.

Technologies used:

* Overall: Java, Spring, Spring Security
* Frontend: Spring MVC, Thymeleaf
* Business: Java
* Backend: JPA/Hibernate, Flyway

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
    
        ($ dropdb 'wms_db')
    
        $ psql -c "CREATE USER wms PASSWORD 'somepassword';"
    
        CREATE ROLE
    
        $ createdb wms_db -O wms

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
         wms_db    | wms      | UTF8     | en_US.UTF-8 | en_US.UTF-8 | 
         (4 rows)

    List tables of database wms_db:

        $ psql -d wms_db
        psql (9.4.1)
        Type "help" for help.

        wms_db=# \d
        No relations found.
        wms_db=# \q

3. Put your database properties into configuration file(s):

        $ cd <WMS source directory>
        $ vi backend/src/main/resources/org/mdz/wms/config/SpringConfigBackend-<profile>.properties

        database.name=wms_db
        database.hostname=localhost
        database.password=somepassword
        database.port=5432
        database.username=wms

## Build

Build CMS:

    $ cd <WMS source directory>
    $ mvn clean install

## Usage

Run CMS (in development)
 
    $ cd frontend
    $ mvn jetty:run

Run CMS (in production)

* Deploy WAR to Tomcat
* Start with java environment variable "spring.profiles.active" set to "PROD" (-Dspring.profiles.active:PROD)

Use CMS

    Browser: http://localhost:9898

CMS connects to database and if no admin user exists, the admin user creation assistant is launched.
Create an admin user.