UPDATE url_aliases SET target_language = 'und' WHERE COALESCE(target_language, '') = '';

