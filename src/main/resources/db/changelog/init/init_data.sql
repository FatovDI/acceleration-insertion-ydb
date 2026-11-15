INSERT INTO currency (code, name, id) VALUES ('RUB', 'Российский рубль', CAST(RandomUuid(1) as Utf8));
INSERT INTO currency (code, name, id) VALUES ('USD', 'Доллар США', CAST(RandomUuid(1) as Utf8));
INSERT INTO currency (code, name, id) VALUES ('CHY', 'Юань', CAST(RandomUuid(1) as Utf8));
INSERT INTO currency (code, name, id) VALUES ('EUR', 'Евро', CAST(RandomUuid(1) as Utf8));

INSERT INTO account (cur, name, number, id) VALUES ('RUB', 'ГПБ, руб.', '00000000000000000001', CAST(RandomUuid(1) as Utf8));
INSERT INTO account (cur, name, number, id) VALUES ('USD', 'ГПБ, usd.', '00000000000000000002', CAST(RandomUuid(1) as Utf8));
INSERT INTO account (cur, name, number, id) VALUES ('EUR', 'ГПБ, eur.', '00000000000000000003', CAST(RandomUuid(1) as Utf8));
INSERT INTO account (cur, name, number, id) VALUES ('RUB', 'СБЕР, руб.', '00000000000000000004', CAST(RandomUuid(1) as Utf8));
INSERT INTO account (cur, name, number, id) VALUES ('CHY', 'СБЕР, юань.', '00000000000000000005', CAST(RandomUuid(1) as Utf8));