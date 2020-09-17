UPDATE fileresources SET http_base_url = (SELECT iiif_base_url FROM fileresources_image WHERE fileresources.uuid = fileresources_image.uuid);
