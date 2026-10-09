-- Initial Opsflow database schema
-- Version: 1

CREATE TABLE app_users (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           created_at DATETIME(6) NOT NULL,
                           email VARCHAR(150) NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           password VARCHAR(100) NOT NULL,
                           role ENUM('ADMIN', 'USER') NOT NULL,
                           PRIMARY KEY (id),
                           CONSTRAINT uk_app_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_records (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   description VARCHAR(500),
                                   operation_date DATE NOT NULL,
                                   operation_time TIME NOT NULL,
                                   status VARCHAR(30) NOT NULL,
                                   title VARCHAR(100) NOT NULL,
                                   PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_history (
                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                   created_at DATETIME(6) NOT NULL,
                                   event_type VARCHAR(30) NOT NULL,
                                   new_status VARCHAR(30),
                                   old_status VARCHAR(30),
                                   operation_id BIGINT NOT NULL,
                                   operation_title VARCHAR(100),
                                   user_id BIGINT,
                                   user_email VARCHAR(150),
                                   user_role VARCHAR(30),
                                   user_name VARCHAR(100),
                                   PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;