# Contributing

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
docker-compose exec database /bin/bash
su - postgres
dropdb -U cudami 'cudami'
createdb -U cudami cudami
docker-compose up
```

If the docker container is running, terminate it with `^C` and delete the `volumes` configuration
from above from `docker-compose.yml`. Afterwards, start the container as usual with `docker-compose up -d`.