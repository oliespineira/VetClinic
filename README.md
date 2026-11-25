classDiagram
    direction TB

    %% ===================== 1. FRANCHISE MANAGEMENT =====================
    namespace Franchise {
        class Clinic {
            -Long id
            -String name
        }
        class Location {
            -Long id
            -String name
            -String address
        }
        class Employee {
            <<abstract>>
            -Long id
            -String name
            -String email
        }
        class Receptionist
        class VetGeneral
        class VetSurgeon
        class ClinicManager
    }

    %% ===================== 2. CLIENTS AND PATIENTS =====================
    namespace ClientsAndPatients {
        class Client {
            -String id
            -String name
            -String phone
            -String email
            -LocalDate registrationDate
        }
        class Animal {
            <<abstract>>
            -String id
            -String name
            -LocalDate dateOfBirth
            -LocalDate registrationDate
            +getCurrentWeightKg() BigDecimal
            +getRequiredVaccines()* List~VaccinationProtocol~
        }
        class CompanionAnimal {
            <<abstract>>
            -String microchipNumber
        }
        class Dog {
            -String breed
        }
        class Cat {
            -String breed
        }
        class Exotic {
            -String speciesName
        }
        class FarmAnimal {
            -String earTagNumber
            -String herdId
            -LivestockType type
            +getSafeToSellDate() LocalDate
        }
    }

    %% ===================== 3. MEDICAL MANAGEMENT =====================
    namespace Medical {
        class Appointment {
            -String id
            -LocalDateTime start
            -int durationMinutes
            -AppointmentType type
            -AppointmentStatus status
        }
        class MedicalHistory
        class MedicalRecord {
            -LocalDate date
            -BigDecimal weightKg
            -String diagnosis
            -String notes
        }
        class Treatment {
            <<abstract>>
            -LocalDate date
            -BigDecimal cost
        }
        class Vaccine {
            -int doseNumber
        }
        class Medication {
            -BigDecimal doseMg
            -int durationDays
            +calculateDose(BigDecimal weightKg) BigDecimal
        }
        class Surgery {
            -String procedure
            -BigDecimal estimatedCost
            -SurgeryStatus status
        }
        class ConsentForm {
            -LocalDateTime signedAt
            -boolean accepted
        }
        class VaccinationProtocol {
            -Species species
            -int initialDoses
            -int weeksBetweenDoses
            -int boosterIntervalMonths
        }
        class VaccinationSchedule {
            -int dosesGiven
            -LocalDate nextDue
            +calculateNextDue() LocalDate
        }
        class Notification {
            -NotificationType type
            -String message
            -LocalDateTime sentAt
        }
    }

    %% ===================== 4. FINANCIAL MANAGEMENT =====================
    namespace Finance {
        class Invoice {
            -String id
            -LocalDate issueDate
            -InvoiceStatus status
            +getTotal() BigDecimal
            +getOutstanding() BigDecimal
        }
        class InvoiceLine {
            -String description
            -int quantity
            -BigDecimal unitPrice
        }
        class Payment {
            -BigDecimal amount
            -LocalDate date
            -PaymentMethod method
        }
        class Expense {
            -BigDecimal amount
            -LocalDate date
            -ExpenseCategory category
        }
        class Supplier {
            -String name
            -String contactEmail
        }
        class Purchase {
            -LocalDate orderDate
            -PurchaseStatus status
        }
    }

    %% ===================== 5. INVENTORY MANAGEMENT =====================
    namespace Inventory_Management {
        class Product {
            -String name
            -ProductType type
            -BigDecimal unitPrice
            -boolean prescriptionRequired
            -BigDecimal dosePerKg
            -int withdrawalDays
        }
        class Inventory {
            -int minimumStock
            +getTotalQuantity() int
            +isLowStock() boolean
        }
        class Batch {
            -String batchNumber
            -LocalDate expiryDate
            -int quantity
            -Long version
            +isExpired() boolean
        }
        class StockMovement {
            -int quantityChange
            -MovementType type
            -LocalDateTime timestamp
        }
    }

    %% ===================== 6. REPORTING (not database tables) =====================
    namespace Reporting {
        class Dashboard {
            <<service>>
        }
        class ClinicReport {
            <<DTO>>
        }
        class PerformanceMetrics {
            <<DTO>>
            -int newAnimals
            -BigDecimal revenue
            -BigDecimal expenses
        }
    }

    %% ---------- Franchise ----------
    Clinic "1" *-- "1..*" Location : has branches
    Location "1" --> "0..*" Employee : employs
    Employee <|-- Receptionist
    Employee <|-- VetGeneral
    Employee <|-- VetSurgeon
    Employee <|-- ClinicManager
    ClinicManager "1..*" --> "1" Clinic : monitors

    %% ---------- Clients and patients ----------
    Client "1" --> "1..*" Animal : owns
    Animal <|-- CompanionAnimal
    Animal <|-- FarmAnimal
    CompanionAnimal <|-- Dog
    CompanionAnimal <|-- Cat
    CompanionAnimal <|-- Exotic

    %% ---------- Medical ----------
    Animal "1" *-- "1" MedicalHistory : has
    MedicalHistory "1" *-- "0..*" MedicalRecord : contains
    MedicalRecord "1" *-- "1..*" Treatment : includes
    Treatment <|-- Vaccine
    Treatment <|-- Medication
    Treatment <|-- Surgery
    Treatment "0..*" --> "1" Employee : performed by
    Surgery "0..*" --> "1" VetSurgeon : performed by
    Surgery "1" --> "1" ConsentForm : requires
    ConsentForm "0..*" --> "1" Client : signed by
    Surgery "1" --> "0..1" Appointment : creates follow-up

    Appointment "0..*" --> "1" Animal : for
    Appointment "0..*" --> "1" Employee : with vet
    Appointment "0..*" --> "1" Location : at
    Appointment "1" --> "0..1" MedicalRecord : results in
    Receptionist ..> Appointment : manages

    Animal "1" --> "0..*" VaccinationSchedule : has
    VaccinationSchedule "0..*" --> "1" VaccinationProtocol : follows
    VaccinationProtocol "0..*" --> "1" Product : vaccine product
    Vaccine ..> VaccinationSchedule : updates dosesGiven and nextDue
    VaccinationSchedule "1" --> "0..*" Notification : triggers 1 week before
    Notification "0..*" --> "0..1" Client : vaccine reminder
    Notification "0..*" --> "0..1" ClinicManager : low-stock alert

    %% ---------- Finance ----------
    Client "1" --> "0..*" Invoice : billed
    Invoice "1" *-- "1..*" InvoiceLine : lines
    InvoiceLine "0..*" --> "0..1" Treatment : for treatment
    InvoiceLine "0..*" --> "0..1" Product : for OTC sale
    Invoice "1" --> "0..*" Payment : paid by
    Receptionist ..> Payment : records
    Receptionist ..> InvoiceLine : sells OTC products
    Location "1" --> "0..*" Expense : spends
    Expense <|-- Purchase
    Purchase "0..*" --> "1" Supplier : from
    Purchase "0..*" --> "1..*" Product : orders

    %% ---------- Inventory ----------
    Location "1" *-- "0..*" Inventory : stocks
    Inventory "0..*" --> "1" Product : of
    Inventory "1" *-- "0..*" Batch : batches
    Batch "1" *-- "0..*" StockMovement : history
    Vaccine "0..*" --> "1" Batch : batch used
    Medication "0..*" --> "1" Batch : batch used
    StockMovement "0..*" --> "0..1" Treatment : caused by
    StockMovement "0..*" --> "0..1" Purchase : restocked by
    StockMovement "0..*" --> "0..1" InvoiceLine : sold in
    Inventory ..> Notification : low stock triggers

    %% ---------- Reporting ----------
    ClinicManager ..> Dashboard : views
    Dashboard ..> ClinicReport : builds
    ClinicReport *-- PerformanceMetrics
    Dashboard ..> Animal : new animals
    Dashboard ..> Payment : revenue
    Dashboard ..> Expense : costs
    Dashboard ..> Inventory : stock levels
