CREATE TABLE IF NOT EXISTS geolocations (
  geolocation_type VARCHAR,
  coordinate_location JSONB
) INHERITS (entities);
