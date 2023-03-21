#! /bin/bash

shopt -s extglob nullglob
cd /docker-entrypoint-initdb.d/

readonly self="${0##*/}"

if [ *.sql ]; then
	# SQL files are processed by postgres itself so we quit here
	# and let postgres proceed.
	echo "+++ $self: .sql file found. $self cancels since there is nothing to do here."
	exit 0
fi

# We use files with suffixes like ".dump.7z", ".sql.bz2" or ".dump"
# but not files ending with e.g. "~". So you can exclude a file from being
# used by renaming it or just switch by symlink.
for file in *.@(dump|sql)?(.7z|.bz2|.gz); do
	! [ -f "$file" ] && continue

	if [[ "$file" =~ \.(dump|sql)(\.[^\.~]+)?$ ]]; then
		[ "${BASH_REMATCH[1]}" == "dump" ] && is_binary=true
		[ "${BASH_REMATCH[2]}" ] && is_archive=true

		declare -a ext_cmd restore_cmd
		[ -v is_archive ] && ext_cmd=(7z x -so "$file") \
		    || ext_cmd=(cat "$file")
		[ -v is_binary ] && restore_cmd=(pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB") \
		    || restore_cmd=(psql "$POSTGRES_DB" "$POSTGRES_USER")

		echo "+++ $self: Restoring database from backup file ${file}..."
		"${ext_cmd[@]}" | "${restore_cmd[@]}"
		echo "+++ $self: Database restored from ${file}"
		exit 0
	fi
done

