#!/bin/bash

pushd src/locales

jq -S '.' de/languages.json > languages_de.json
mv languages_de.json de/languages.json

jq -S '.' de/translation.json > translation_de.json
mv translation_de.json de/translation.json

jq -S '.' en/languages.json > languages_en.json
mv languages_en.json en/languages.json

jq -S '.' en/translation.json > translation_en.json
mv translation_en.json en/translation.json

popd
