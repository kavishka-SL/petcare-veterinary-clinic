-- PetCare Veterinary Clinic Management System
-- Step 3: Database and table creation

CREATE DATABASE IF NOT EXISTS vet_clinic
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vet_clinic;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST', 'VETERINARIAN') NOT NULL,
    veterinarian_id INT UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(120),
    address VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pets (
    pet_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    pet_name VARCHAR(60) NOT NULL,
    species VARCHAR(40) NOT NULL,
    breed VARCHAR(60),
    date_of_birth DATE,
    sex ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
    CONSTRAINT fk_pets_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE veterinarians (
    veterinarian_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(120),
    specialization VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_veterinarian
    FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(veterinarian_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

CREATE TABLE services (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_services_price CHECK (price >= 0)
);

CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    pet_id INT NOT NULL,
    veterinarian_id INT NOT NULL,
    service_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    symptoms VARCHAR(255),
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED')
        NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointments_pet
        FOREIGN KEY (pet_id) REFERENCES pets(pet_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_veterinarian
        FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(veterinarian_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_service
        FOREIGN KEY (service_id) REFERENCES services(service_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_veterinarian_schedule
        (veterinarian_id, appointment_date, appointment_time)
);

CREATE TABLE treatments (
    treatment_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    medication VARCHAR(255),
    notes VARCHAR(500),
    treatment_date DATE NOT NULL,
    CONSTRAINT fk_treatments_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Price catalogue used when adding charges to an appointment.
-- Examples include consultation fees, vaccines and medicines.
CREATE TABLE billable_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT NOT NULL,
    item_type ENUM('CONSULTATION', 'VACCINE', 'MEDICINE', 'PROCEDURE') NOT NULL,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    unit_name VARCHAR(30) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_billable_items_price CHECK (unit_price >= 0),
    CONSTRAINT fk_billable_items_service
        FOREIGN KEY (service_id) REFERENCES services(service_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Stores the actual items used for one appointment.
-- unit_price is saved here so an old bill does not change when catalogue prices change.
CREATE TABLE appointment_items (
    appointment_item_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_appointment_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_appointment_items_price CHECK (unit_price >= 0),
    CONSTRAINT uq_appointment_item UNIQUE (appointment_id, item_id),
    CONSTRAINT fk_appointment_items_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_appointment_items_item
        FOREIGN KEY (item_id) REFERENCES billable_items(item_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD') NOT NULL,
    payment_date DATE NOT NULL,
    CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    CONSTRAINT fk_payments_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Starter records for testing dropdown lists later.
INSERT INTO veterinarians
    (full_name, phone, email, specialization)
VALUES
    ('Dr. Amali Perera', '0771234567', 'amali@petcare.lk', 'General Veterinary Care'),
    ('Dr. Nimal Silva', '0719876543', 'nimal@petcare.lk', 'Pet Surgery');

-- Starter login accounts used to demonstrate role-based access.
-- Passwords are plain text only for this coursework demonstration.
INSERT INTO users
    (username, password, role, veterinarian_id, active)
VALUES
    ('admin', 'admin123', 'ADMIN', NULL, TRUE),
    ('reception', 'reception123', 'RECEPTIONIST', NULL, TRUE),
    ('vet.amali', 'vet123', 'VETERINARIAN', 1, TRUE);

INSERT INTO services
    (service_name, description, price)
VALUES
    ('General Consultation', 'Routine veterinary consultation', 2500.00),
    ('Vaccination', 'Vaccination service selected according to the pet', 0.00),
    ('Treatment / Medical Care', 'Medical treatment selected according to the pet', 0.00);

-- Sample price schedule. Staff can change these prices later from the price schedule screen.
INSERT INTO billable_items
    (service_id, item_type, item_name, unit_name, unit_price)
VALUES
    (1, 'CONSULTATION', 'General Consultation Fee', 'Visit', 2500.00),
    (2, 'VACCINE', 'Rabies Vaccine', 'Dose', 3000.00),
    (2, 'VACCINE', 'DHPP Vaccine', 'Dose', 3500.00),
    (2, 'VACCINE', 'FVRCP Vaccine', 'Dose', 3200.00),
    (3, 'MEDICINE', 'Antibiotic Tablet', 'Tablet', 100.00),
    (3, 'MEDICINE', 'Deworming Medicine', 'Dose', 500.00),
    (3, 'PROCEDURE', 'Wound Dressing', 'Procedure', 1500.00),
    (3, 'PROCEDURE', 'Injection Administration', 'Injection', 800.00);

-- Example JOIN for the appointment screen and Jasper report.
SELECT
    a.appointment_id,
    c.full_name AS customer_name,
    p.pet_name,
    v.full_name AS veterinarian_name,
    s.service_name,
    s.price,
    a.appointment_date,
    a.appointment_time,
    a.status
FROM appointments a
INNER JOIN pets p
    ON a.pet_id = p.pet_id
INNER JOIN customers c
    ON p.customer_id = c.customer_id
INNER JOIN veterinarians v
    ON a.veterinarian_id = v.veterinarian_id
INNER JOIN services s
    ON a.service_id = s.service_id
ORDER BY a.appointment_date, a.appointment_time;
