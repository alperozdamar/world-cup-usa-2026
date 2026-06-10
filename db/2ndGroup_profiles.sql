-- Run after the app has started once (user_profiles table is created by Hibernate).
INSERT INTO user_profiles (username, timezone_id, display_name, email) VALUES
        ('alper', 'America/New_York', 'Alper Ozdamar', NULL),
        ('emre', 'America/New_York', 'Emre', NULL),
        ('can', 'America/New_York', 'Can Tekyetim', NULL),
        ('caglar', 'America/New_York', 'Caglar Panus', NULL),
        ('ozcan', 'America/New_York', 'Ozcan Sakir', NULL)
ON DUPLICATE KEY UPDATE
        display_name = VALUES(display_name),
        timezone_id = VALUES(timezone_id);
