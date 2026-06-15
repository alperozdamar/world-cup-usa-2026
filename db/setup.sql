CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` char(68) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `users` (`username`, `password`, `enabled`) VALUES
        ('alper', '{noop}123', 1),
        ('gonenc', '{noop}123', 1),
        ('tcan', '{noop}123', 1),
        ('kubilay', '{noop}123', 1),
        ('ali', '{noop}123', 1),
        ('sadik', '{noop}123', 1),
        ('adem', '{noop}123', 1);

CREATE TABLE IF NOT EXISTS `authorities` (
  `username` varchar(50) NOT NULL,
  `authority` varchar(50) NOT NULL,
  UNIQUE KEY `authorities_idx_1` (`username`,`authority`),
  CONSTRAINT `FK_authorities_username_users_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `authorities` (`username`, `authority`) VALUES
        ('alper', 'ROLE_ADMIN'),
        ('alper', 'ROLE_USER'),
        ('gonenc', 'ROLE_USER'),
        ('tcan', 'ROLE_USER'),
        ('kubilay', 'ROLE_USER'),
        ('ali', 'ROLE_USER'),
        ('sadik', 'ROLE_USER'),
        ('adem', 'ROLE_USER');

CREATE TABLE IF NOT EXISTS `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
