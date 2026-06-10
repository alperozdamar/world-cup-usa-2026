CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` char(68) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `users` (`username`, `password`, `enabled`) VALUES
        ('alper', '{noop}123', 1),
        ('emre', '{noop}123', 1),
        ('can', '{noop}123', 1),
        ('caglar', '{noop}123', 1),
        ('ozcan', '{noop}123', 1);

CREATE TABLE IF NOT EXISTS `authorities` (
  `username` varchar(50) NOT NULL,
  `authority` varchar(50) NOT NULL,
  UNIQUE KEY `authorities_idx_1` (`username`,`authority`),
  CONSTRAINT `FK_authorities_username_users_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `authorities` (`username`, `authority`) VALUES
        ('alper', 'ROLE_ADMIN'),
        ('alper', 'ROLE_USER'),
        ('emre', 'ROLE_USER'),
        ('can', 'ROLE_USER'),
        ('caglar', 'ROLE_USER'),
        ('ozcan', 'ROLE_USER');
