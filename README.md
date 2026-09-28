\# PetCare Veterinary Clinic



PetCare is a Java Swing desktop application for managing the daily operations of a veterinary clinic.



\## Main Features



\- Customer and pet management

\- Veterinarian management

\- Appointment scheduling

\- Treatment recording

\- Price schedule management

\- Appointment billing and payment recording

\- Role-based access for administrators, receptionists, and veterinarians

\- JasperReports for clinic revenue, appointments, treatments, and billable items



\## Technologies



\- Java Swing

\- MySQL / MariaDB

\- JDBC

\- JasperReports 7.0.3

\- MySQL Connector/J 8.0.30

\- Apache NetBeans

\- XAMPP



\## Database Setup



1\. Start MySQL using XAMPP.

2\. Open phpMyAdmin.

3\. Import `database/vet\_clinic.sql`.

4\. Confirm that the database is named `vet\_clinic`.



\## Demo Accounts



| Role | Username | Password |

|---|---|---|

| Administrator | admin | admin123 |

| Receptionist | reception | reception123 |

| Veterinarian | vet.amali | vet123 |



Additional veterinarian accounts can be inserted manually into the `users` table and linked using `veterinarian\_id`.



\## Reports



The Administrator can generate:



\- Clinic Revenue and Service Report

\- Appointment Schedule Report

\- Treatment History Report

\- Billable Item Usage Report



\## Project Information



This application was developed for the Enterprise Application Development module coursework.

