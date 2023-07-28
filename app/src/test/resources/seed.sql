create table IF NOT EXISTS url (
  id                            bigint generated by default as identity not null,
  name                          varchar(255),
  created_at                    timestamp not null,
  constraint pk_url primary key (id)
);

INSERT INTO url (name, created_at) VALUES
	('http://yandex.ru', CURRENT_TIMESTAMP()),
	('http://fontanka.ru', CURRENT_TIMESTAMP());