CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` char(68) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `users` (`username`, `password`, `enabled`) VALUES
        ('alper', '{noop}123', 1),
        ('john', '{bcrypt}$2y$12$2uyIQbVvq2STqGm8BZZpy.NHqm2KHyH8OrItzqDXk.AsiuPdu.D4O', 1),
        ('mary', '{bcrypt}$2a$04$eFytJDGtjbThXa80FyOOBuFdK2IwjyWefYkMpiBEFlpBwDH.5PM0K', 1),
        ('susan', '{noop}test123', 1);

CREATE TABLE IF NOT EXISTS `authorities` (
  `username` varchar(50) NOT NULL,
  `authority` varchar(50) NOT NULL,
  UNIQUE KEY `authorities_idx_1` (`username`,`authority`),
  CONSTRAINT `FK_authorities_username_users_username` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `authorities` (`username`, `authority`) VALUES
        ('alper', 'ROLE_ADMIN'),
        ('alper', 'ROLE_USER'),
        ('john', 'ROLE_USER'),
        ('mary', 'ROLE_USER'),
        ('susan', 'ROLE_ADMIN'),
        ('susan', 'ROLE_USER');
