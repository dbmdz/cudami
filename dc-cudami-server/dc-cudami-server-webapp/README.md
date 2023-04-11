# cudami Repository Server

## Development

### Import database dump into Docker development environment

See <https://simkimsia.com/how-to-restore-database-dumps-for-postgres-in-docker-container/>

Lookup outside directory suitable for reading database dump from within docker environment:

* Start development docker environment:

```sh
$ cd docker/repository
$ docker compose up -d
```

* List mounted volume paths under the key Destination:

```sh
$ docker inspect -f '{{ json .Mounts }}' cudami-repository-database-1
[{"Type":"volume","Name":"4cdb98f188ad1940f0ecf2eedf1007c0689e6802e249089ef8ae87b6e9fa0582","Source":"/var/lib/docker/volumes/4cdb98f188ad1940f0ecf2eedf1007c0689e6802e249089ef8ae87b6e9fa0582/_data","Destination":"/var/lib/postgresql/data","Driver":"local","Mode":"","RW":true,"Propagation":""}]
```

In this case `/var/lib/postgresql/data` is the volume path.

* Copy dump into one of the volumes

Run docker cp </path/to/dump/in/host> <container_name>:<path_to_volume>

In above case:

```sh
$ bunzip2 postgresql-dump-cudami-230221-1103.bz2
$ docker cp postgresql-dump-cudami-230221-1103 cudami-repository-database-1:/var/lib/postgresql/data
...
Copying to container - 11.68GB
Successfully copied 11.68GB to cudami-repository-database-1:/var/lib/postgresql/data
```

* Get the database owner

See docker-compose.yml:

```yml
environment:
      POSTGRES_USER: "cudami"
      POSTGRES_PASSWORD: "somepassword"
      POSTGRES_DB: "cudami"
```

List databases for user `cudami`:

```sh
$ docker exec cudami-repository-database-1 psql -U cudami -l
                              List of databases
   Name    | Owner  | Encoding |  Collate   |   Ctype    | Access privileges 
-----------+--------+----------+------------+------------+-------------------
 cudami    | cudami | UTF8     | en_US.utf8 | en_US.utf8 | 
 postgres  | cudami | UTF8     | en_US.utf8 | en_US.utf8 | 
 template0 | cudami | UTF8     | en_US.utf8 | en_US.utf8 | =c/cudami        +
           |        |          |            |            | cudami=CTc/cudami
 template1 | cudami | UTF8     | en_US.utf8 | en_US.utf8 | =c/cudami        +
           |        |          |            |            | cudami=CTc/cudami
(4 rows)
```

* Restore data from dump

Start from scratch:

```sh
$ docker compose down
[+] Running 4/4
 ⠿ Container cudami-repository-media-1     Removed
 ⠿ Container cudami-repository-database-1  Removed
 ⠿ Container cudami-repository-iiif-1      Removed
 ⠿ Network cudami_default       Removed 
$ docker compose up -d
[+] Running 4/4
 ⠿ Network cudami_default       Created
 ⠿ Container cudami-repository-iiif-1      Started
 ⠿ Container cudami-repository-database-1  Started
 ⠿ Container cudami-repository-media-1     Started
```

Restore from local SQL file:

```sh
$ docker exec -i cudami-repository-database-1 /bin/bash -c "PGPASSWORD=somepassword psql --username cudami cudami" < <your_local_directory_here>/postgresql-dump-cudami-230221-1103
```

Not working? (with pg_restore from copied dump):

```sh
$ docker exec cudami-repository-database-1 pg_restore -U cudami -d cudami /var/lib/postgresql/data/postgresql-dump-cudami-230221-1103
```
