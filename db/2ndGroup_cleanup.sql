-- Remove Group 1 users accidentally seeded on the Group 2 server (run once).
-- Safe to run only on the Group 2 database.
-- Run after the app has started at least once (Hibernate creates these tables).

SET @pool = 'alper,emre,can,caglar,ozcan';

DELETE FROM prediction_audits WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM predictions WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM group_standing_prediction_audits WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM group_standing_predictions WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM final_prediction_audits WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM final_predictions WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM user_comments WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM user_profiles WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM authorities WHERE FIND_IN_SET(username, @pool) = 0;
DELETE FROM users WHERE FIND_IN_SET(username, @pool) = 0;
