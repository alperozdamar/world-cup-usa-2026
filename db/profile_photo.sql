-- Profile photos up to 2 MB need MEDIUMBLOB (default Hibernate BLOB is only 64 KB).
-- Run once on existing databases after deploying profile photo upload.

ALTER TABLE user_profiles
    MODIFY COLUMN photo_data MEDIUMBLOB NULL;
