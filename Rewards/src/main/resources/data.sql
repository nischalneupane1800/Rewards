INSERT INTO CUSTOMER (id, name) VALUES ('c1', 'David');
INSERT INTO CUSTOMER (id, name) VALUES ('c2', 'Alex');
INSERT INTO CUSTOMER (id, name) VALUES ('c3', 'Maria');
INSERT INTO CUSTOMER (id, name) VALUES ('c4', 'Chen');


INSERT INTO TRANSACTION (id, customer_id, amount, date) VALUES
('t1', 'c1',  49.99, DATE '2025-12-05'),
('t2', 'c1',  50.00, DATE '2025-12-06'),
('t3', 'c1',  51.00, DATE '2025-12-07'),
('t4', 'c1', 100.00, DATE '2026-01-10'),
('t5', 'c1', 120.00, DATE '2026-01-15'),
('t6', 'c1', 250.00, DATE '2026-02-01');

INSERT INTO TRANSACTION (id, customer_id, amount, date) VALUES
('t7',  'c2',  75.00, DATE '2025-12-03'),
('t8',  'c2', 200.00, DATE '2026-01-12'),
('t9',  'c2', 500.00, DATE '2026-02-20');

INSERT INTO TRANSACTION (id, customer_id, amount, date) VALUES
('t10', 'c3',   0.00, DATE '2026-02-10'),
('t11', 'c3',  49.00, DATE '2026-02-11'),
('t12', 'c3',  50.00, DATE '2026-02-12');

INSERT INTO TRANSACTION (id, customer_id, amount, date) VALUES
('t13', 'c4', 1000.00, DATE '2026-01-01');
