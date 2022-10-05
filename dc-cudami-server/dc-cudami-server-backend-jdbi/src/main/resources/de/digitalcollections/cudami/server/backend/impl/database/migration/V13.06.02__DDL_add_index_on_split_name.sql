CREATE INDEX idx_agents_split_name
  ON agents USING gin (split_name);
CREATE INDEX idx_corporatebodies_split_name
  ON corporatebodies USING gin (split_name);
CREATE INDEX idx_persons_split_name
  ON persons USING gin (split_name);
CREATE INDEX idx_geolocations_split_name
  ON geolocations USING gin (split_name);
CREATE INDEX idx_humansettlements_split_name
  ON humansettlements USING gin (split_name);
