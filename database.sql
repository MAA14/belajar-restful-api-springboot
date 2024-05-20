CREATE TABLE users
(
    username varchar(100) not null,
    password varchar(100) not null,
    name varchar(100) not null,
    token varchar(100),
    token_expired_at bigint,
    primary key (username),
    unique (token)
) ENGINE=InnoDB;

SELECT * FROM users;
DESC users;

create table contacts(
    id varchar(100) not null,
    username varchar(100) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(100) not null,
    phone varchar(100) not null,
    primary key (id),
    foreign key fk_users_contacts (username) REFERENCES users (username)
) ENGINE=InnoDB;

SELECT * FROM contacts;
DESC contacts;

CREATE TABLE addresses (
    id varchar(100) not null,
    contact_id varchar(100) not null,
    street varchar(200),
    city varchar(100),
    province varchar(100),
    country varchar(100) not null,
    postal_code varchar(10),
    primary key (id),
    foreign key fk_contacts_addresses (contact_id) REFERENCES contacts (id)
) ENGINE=InnoDB;

select * from addresses;
desc addresses;

