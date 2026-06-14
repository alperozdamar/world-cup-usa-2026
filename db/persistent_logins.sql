-- Spring Security remember-me tokens (created automatically on startup if missing).
CREATE TABLE IF NOT EXISTS persistent_logins (
    username varchar(64) NOT NULL,
    series varchar(64) NOT NULL,
    token varchar(64) NOT NULL,
    last_used timestamp NOT NULL,
    PRIMARY KEY (series)
);
