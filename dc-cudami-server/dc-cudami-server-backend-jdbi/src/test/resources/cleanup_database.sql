-- The order is important due to foreign keys!
delete from url_aliases;
delete from rel_entity_entities;
delete from predicates;

delete from article_creators;
delete from articles;
delete from collection_collections;
delete from collection_digitalobjects;
delete from collections;
delete from corporatebodies;
delete from digitalobject_fileresources;
delete from digitalobject_linkeddataresources;
delete from digitalobject_renderingresources;
delete from digitalobjects;
delete from entities;
delete from familynames;
delete from fileresources;
delete from fileresources_application;
delete from fileresources_audio;
delete from fileresources_image;
delete from fileresources_linkeddata;
delete from fileresources_text;
delete from fileresources_video;
delete from geolocations;
delete from givennames;
delete from headwordentries;
delete from headwordentry_creators;
delete from headwords;
delete from humansettlements;
delete from identifiables;
delete from identifiers;
delete from identifiertypes;
delete from item_works;
delete from items;
delete from licenses;
delete from persistent_logins;
delete from person_familynames;
delete from person_givennames;
delete from persons;
delete from project_digitalobjects;
delete from projects;
delete from rel_entity_fileresources;
delete from rel_identifiable_entities;
delete from rel_identifiable_fileresources;
delete from rendering_templates;
delete from subjects;
delete from tags;
delete from topic_entities;
delete from topic_fileresources;
delete from topic_topics;
delete from topics;
delete from users;
delete from versions;
delete from webpage_webpages;
delete from webpages;
delete from website_webpages;
delete from websites;
delete from work_creators;
delete from works;