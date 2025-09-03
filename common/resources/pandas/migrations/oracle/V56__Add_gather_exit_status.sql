-- Add exit status column to INS_GATHER table
ALTER TABLE INS_GATHER ADD EXIT_STATUS NUMBER(10);

-- Add comment to the column
COMMENT ON COLUMN INS_GATHER.EXIT_STATUS IS 'Exit status code returned by the crawler process';