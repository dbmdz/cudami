CREATE TABLE manifestation_publishers (
  manifestation_uuid uuid NOT NULL CONSTRAINT fk_manifestation REFERENCES manifestations,
  publisher_uuid uuid NOT NULL CONSTRAINT fk_publisher REFERENCES publishers,
  sortKey smallint NOT NULL,

  PRIMARY KEY (manifestation_uuid, publisher_uuid)
);

