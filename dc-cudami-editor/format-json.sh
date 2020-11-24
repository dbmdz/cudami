#!/bin/bash

# Determine needed directory path
json_dir=$(pwd)/public/__mock__
formatted_json_dir=$json_dir/formatted

# Create needed directories
mkdir -p $formatted_json_dir/new

# Pretty-print the json files
for json_path in $(find $json_dir -iname '*.json'); do
  formatted_json_path=$formatted_json_dir/$(realpath --relative-to=$json_dir $json_path)
  jq -S '.' $json_path > $formatted_json_path
  cp $formatted_json_path $json_path
done

# Remove obsolete directory
rm -rf $formatted_json_dir
