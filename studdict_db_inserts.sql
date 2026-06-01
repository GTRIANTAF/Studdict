-- ΑΠΕΝΕΡΓΟΠΟΙΗΣΗ SAFE UPDATES ΓΙΑ ΚΑΘΑΡΙΣΜΟ
SET SQL_SAFE_UPDATES = 0;

-- ΔΙΑΓΡΑΦΗ ΠΑΛΙΩΝ ΔΕΔΟΜΕΝΩΝ (Με τη σωστή σειρά για να μην χτυπήσουν τα Foreign Keys)
DELETE FROM payments;
DELETE FROM points_transactions;
DELETE FROM ebook_loans;
DELETE FROM check_ins;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM loyalty_wallets;
DELETE FROM menu_items;
DELETE FROM bills;
DELETE FROM invite_codes;
DELETE FROM reservation_participants;
DELETE FROM private_reservations;
DELETE FROM public_reservations;
DELETE FROM reservations;
DELETE FROM study_subjects;
DELETE FROM study_tables;
DELETE FROM students;
DELETE FROM venues;

-- ΕΠΑΝΑΦΟΡΑ ΤΟΥ SAFE UPDATES
SET SQL_SAFE_UPDATES = 1;

-- ΕΠΑΝΑΦΟΡΑ ΤΩΝ ID (Auto-Increment) ΣΤΟ 1
ALTER TABLE venues AUTO_INCREMENT = 1;
ALTER TABLE study_tables AUTO_INCREMENT = 1;
ALTER TABLE menu_items AUTO_INCREMENT = 1;
ALTER TABLE study_subjects AUTO_INCREMENT = 1;

-- 1. ΕΙΣΑΓΩΓΗ VENUES (Καταστήματα)
INSERT INTO venues (name, address) VALUES 
('Library Core', 'Downtown Campus'),
('Silent Hub', 'North Wing');

-- 2. ΕΙΣΑΓΩΓΗ STUDY TABLES (Όλα είναι is_available = 1 εξ' ορισμού)
INSERT INTO study_tables (venue_id, table_number, capacity, qr_code_string, is_available) VALUES 
(1, 101, 2, 'QR_CORE_101', 1),
(1, 102, 4, 'QR_CORE_102', 1),
(1, 103, 6, 'QR_CORE_103', 1),
(2, 201, 1, 'QR_HUB_201', 1),
(2, 202, 4, 'QR_HUB_202', 1);

-- 3. ΕΙΣΑΓΩΓΗ MENU ITEMS (Για τις παραγγελίες)
INSERT INTO menu_items (name, price, is_available, category) VALUES 
('Espresso', 2.50, 1, 'Beverage'),
('Cappuccino', 3.00, 1, 'Beverage'),
('Club Sandwich', 6.00, 1, 'Food'),
('Croissant', 2.00, 1, 'Snack');

-- 4. ΕΙΣΑΓΩΓΗ SUBJECTS (Για τα Public/Matchmaking Reservations)
INSERT INTO study_subjects (name) VALUES 
('Mathematics'),
('Computer Science'),
('Physics'),
('History');

-- 5. ΕΙΣΑΓΩΓΗ STUDENTS (O 'S1' είναι το default ID που χρησιμοποιεί το Android App)
INSERT INTO students (student_id, first_name, last_name, email, password, university, department) VALUES 
('S1', 'Giannis', 'Triantafyllou', 'giannis@studdict.com', '1234', 'AUEB', 'Informatics'),
('S2', 'Maria', 'K.', 'maria@studdict.com', '1234', 'AUEB', 'Management');

-- 6. ΕΙΣΑΓΩΓΗ WALLETS (Με 200 πόντους για να μπορείς να τεστάρεις το Redeem Points)
INSERT INTO loyalty_wallets (student_id, total_balance, minimum_redeem_limit, exchange_rate) VALUES 
('S1', 200, 25, 0.03),
('S2', 100, 25, 0.03);