# Contributing

## Development

### Environment

#### Docker

Install `docker-compose` for running development container

```
$ sudo apt install docker-compose
```

Change to subdirectory `docker` to execute local Docker based environment containing PostgreSql database (Port 22222), Hymir IIIF server (Port 23232) and Euphoria Streaming server (Port 24242).

```
$ cd docker
$ docker-compose up -d
Creating cudami_media_1    ... done                                                                                                                                                                                             
Creating cudami_database_1 ... done                                                                                                                                                                                             
Creating cudami_iiif_1     ... done
$
```

## Restore an existing backup into the local docker instance

Copy the dump SQL, e.g. `postgresql-dump-cudami-210906-0112` into `cudami/docker`, then
append the following lines into the `database` section in `docker-compose.yml`:
```yml
  volumes:
    - ./postgresql-dump-cudami-210525-0112:/docker-entrypoint-initdb.d/dump.sql
```

Now run the following commands within `cudami/docker`:

```sh
docker-compose down
docker-compose up
```

If the docker container is running, terminate it with `^C` and delete the `volumes` configuration
from above from `docker-compose.yml`. Afterwards, start the container as usual with `docker-compose up -d`.