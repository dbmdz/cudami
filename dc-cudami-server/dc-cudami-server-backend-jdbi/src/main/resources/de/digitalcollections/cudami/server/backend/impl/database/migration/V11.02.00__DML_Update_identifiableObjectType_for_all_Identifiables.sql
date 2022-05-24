UPDATE identifiables SET identifiable_objecttype = 'IDENTIFIABLE';
UPDATE entities SET identifiable_objecttype = 'ENTITY';

UPDATE articles SET identifiable_objecttype = 'ARTICLE';
UPDATE collections SET identifiable_objecttype = 'COLLECTION';
UPDATE corporatebodies SET identifiable_objecttype = 'CORPORATE_BODY';
UPDATE digitalobjects SET identifiable_objecttype = 'DIGITAL_OBJECT';
UPDATE familynames SET identifiable_objecttype = 'FAMILY_NAME';
UPDATE givennames SET identifiable_objecttype = 'GIVEN_NAME';
UPDATE headwordentries SET identifiable_objecttype = 'HEADWORD_ENTRY';
UPDATE items SET identifiable_objecttype = 'ITEM';
UPDATE persons SET identifiable_objecttype = 'PERSON';
UPDATE projects SET identifiable_objecttype = 'PROJECT';
UPDATE topics SET identifiable_objecttype = 'TOPIC';
UPDATE websites SET identifiable_objecttype = 'WEBSITE';
UPDATE works SET identifiable_objecttype = 'WORK';

UPDATE geolocations SET identifiable_objecttype = 'GEO_LOCATION';
UPDATE geolocations SET identifiable_objecttype = 'CANYON' WHERE geolocation_type = 'CANYON';
UPDATE geolocations SET identifiable_objecttype = 'CAVE' WHERE geolocation_type = 'CAVE';
UPDATE geolocations SET identifiable_objecttype = 'CONTINENT' WHERE geolocation_type = 'CONTINENT';
UPDATE geolocations SET identifiable_objecttype = 'COUNTRY' WHERE geolocation_type = 'COUNTRY';
UPDATE geolocations SET identifiable_objecttype = 'CREEK' WHERE geolocation_type = 'CREEK';
UPDATE geolocations SET identifiable_objecttype = 'LAKE' WHERE geolocation_type = 'LAKE';
UPDATE geolocations SET identifiable_objecttype = 'MOUNTAIN' WHERE geolocation_type = 'MOUNTAIN';
UPDATE geolocations SET identifiable_objecttype = 'OCEAN' WHERE geolocation_type = 'OCEAN';
UPDATE geolocations SET identifiable_objecttype = 'RIVER' WHERE geolocation_type = 'RIVER';
UPDATE geolocations SET identifiable_objecttype = 'SEA' WHERE geolocation_type = 'SEA';
UPDATE geolocations SET identifiable_objecttype = 'STILL_WATERS' WHERE geolocation_type = 'STILL_WATERS';
UPDATE geolocations SET identifiable_objecttype = 'VALLEY' WHERE geolocation_type = 'VALLEY';

UPDATE humansettlements SET identifiable_objecttype = 'HUMAN_SETTLEMENT';

UPDATE fileresources SET identifiable_objecttype = 'FILE_RESOURCE';
UPDATE fileresources_application SET identifiable_objecttype = 'APPLICATION_FILE_RESOURCE';
UPDATE fileresources_audio SET identifiable_objecttype = 'AUDIO_FILE_RESOURCE';
UPDATE fileresources_image SET identifiable_objecttype = 'IMAGE_FILE_RESOURCE';
UPDATE fileresources_linkeddata SET identifiable_objecttype = 'LINKED_DATA_FILE_RESOURCE';
UPDATE fileresources_text SET identifiable_objecttype = 'TEXT_FILE_RESOURCE';
UPDATE fileresources_video SET identifiable_objecttype = 'VIDEO_FILE_RESOURCE';

UPDATE webpages SET identifiable_objecttype = 'WEBPAGE';

-- no tables, yet:
--  AGENT(Agent.class),
--  EXPRESSION(Expression.class),
--  FAMILY(Family.class),
--  MANIFESTATION(Manifestation.class),
