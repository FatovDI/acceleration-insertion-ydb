CREATE TABLE currency
(
    code utf8,
    name utf8 NULL,
    id utf8 NOT NULL,
    primary key (code)
);

CREATE TABLE account
(
    name utf8,
    number utf8,
    cur utf8,
    id utf8 NOT NULL,
    primary key (id)
);

CREATE TABLE payment_document
(
    account_id utf8,
    amount decimal(22, 9),
    expense bool,
    cur utf8,
    order_date date,
    order_number utf8,
    payment_purpose utf8,
    prop_10 utf8,
    prop_15 utf8,
    prop_20 utf8,
    id utf8 NOT NULL,
    primary key (id)
);

ALTER TABLE `payment_document` ADD INDEX `IX_payment_document_cur` GLOBAL ON (cur);
ALTER TABLE `payment_document` ADD INDEX `IX_payment_document_order_date` GLOBAL ON (order_date);
ALTER TABLE `payment_document` ADD INDEX `IX_payment_document_order_number` GLOBAL ON (order_number);