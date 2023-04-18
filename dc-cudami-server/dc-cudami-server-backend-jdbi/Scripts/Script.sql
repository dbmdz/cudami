select
  f.uuid fr_uuid,
  f.created fr_created,
  f.last_modified fr_lastModified,
  f.description fr_description,
  f.identifiable_objecttype fr_identifiableObjectType,
  f.identifiable_type fr_type,
  f.label fr_label,
  f.preview_hints fr_previewImageRenderingHints,
  f.filename fr_filename,
  f.http_base_url fr_httpBaseUrl,
  f.mimetype fr_mimeType,
  f.size_in_bytes fr_sizeInBytes,
  f.uri fr_uri,
  id.uuid id_uuid,
  id.created id_created,
  id.last_modified id_lastModified,
  id.identifiable id_identifiable,
  id.namespace id_namespace,
  id.identifier id_id,
  file.uuid pi_uuid,
  file.filename pi_filename,
  file.mimetype pi_mimeType,
  file.uri pi_uri,
  file.http_base_url pi_httpBaseUrl,
  ua.uuid ua_uuid,
  ua.created ua_created,
  ua.last_modified ua_lastModified,
  ua.last_published ua_lastPublished,
  ua.primary ua_primary,
  ua.slug ua_slug,
  ua.target_language ua_targetLanguage,
  ua.target_identifiable_objecttype uaidf_identifiableObjectType,
  ua.target_identifiable_type uaidf_identifiableType,
  ua.target_uuid uaidf_uuid,
  uawebs.uuid uawebs_uuid,
  uawebs.label uawebs_label,
  uawebs.url uawebs_url
from
  (
  select
    xtable.sortindex as idx,
    *
  from
    fileresources as f
  inner join topic_fileresources as xtable on
    f.uuid = xtable.fileresource_uuid
  where
    xtable.topic_uuid = :uuid
  order by
    lower(coalesce(f.label->>'en', f.label->>'')) collate "ucs_basic" asc
  limit 10 offset 0) as f
left join identifiers as id on
  f.uuid = id.identifiable
left join fileresources_image as file on
  f.previewfileresource = file.uuid
left join url_aliases as ua on
  f.uuid = ua.target_uuid
left join websites as uawebs on
  uawebs.uuid = ua.website_uuid
order by
  lower(coalesce(f.label->>'en', f.label->>'')) collate "ucs_basic" asc