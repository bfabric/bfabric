ALTER ROLE bfabric WITH PASSWORD 'changeit' VALID UNTIL 'infinity';

UPDATE user_
SET password = 'changeit';