select
  c.uuid cb_uuid,
  c.created cb_created,
  c.last_modified cb_lastModified,
  c.description cb_description,
  c.identifiable_objecttype cb_identifiableObjectType,
  c.identifiable_type cb_type,
  c.label cb_label,
  c.preview_hints cb_previewImageRenderingHints,
  c.custom_attrs cb_customAttributes,
  c.navdate cb_navDate,
  c.refid cb_refId,
  c.notes cb_notes,
  c.name cb_name,
  c.name_locales_original_scripts cb_nameLocalesOfOriginalScripts,
  c.homepage_url cb_homepageUrl,
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
    c.*
  from
    corporatebodies as c
  where
    c.split_name::text[] @> ?::text[]
  order by
    lower(c.label) asc
  limit 100000 offset 0) as c
left join identifiers as id on
  c.uuid = id.identifiable
left join fileresources_image as file on
  c.previewfileresource = file.uuid
left join url_aliases as ua on
  c.uuid = ua.target_uuid
left join websites as uawebs on
  uawebs.uuid = ua.website_uuid
order by
  lower(c.label) asc;