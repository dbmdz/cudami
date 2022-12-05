ALTER TABLE manifestations
  DROP COLUMN IF EXISTS publishingdatepresentation,
  DROP COLUMN IF EXISTS publishingdaterange,
  DROP COLUMN IF EXISTS publishing_timevaluerange,
  -- publicationInfo
  ADD COLUMN IF NOT EXISTS publication_info jsonb,
  ADD COLUMN IF NOT EXISTS publication_nav_date daterange,
  -- productionInfo
  ADD COLUMN IF NOT EXISTS production_info jsonb,
  ADD COLUMN IF NOT EXISTS production_nav_date daterange,
  -- distributionInfo
  ADD COLUMN IF NOT EXISTS distribution_info jsonb,
  ADD COLUMN IF NOT EXISTS distribution_nav_date daterange;

