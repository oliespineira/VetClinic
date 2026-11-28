# VetClinic – User Guide

## 1. Logging in
VetClinic is a REST API. Every request is made as a logged-in employee using HTTP Basic authentication (email and password). Your role is determined by your employee type and decides what you can do.

| Role | Email | Password |
|---|---|---|
| Receptionist | reception@pawcare.example | reception123 |
| General vet | vet@pawcare.example | vet123 |
| Surgeon | surgeon@pawcare.example | surgeon123 |
| Clinic manager | manager@pawcare.example | manager123 |

- **Postman:** Authorization tab → Type: Basic Auth → enter email and password.
- **curl:** `curl -u vet@pawcare.example:vet123 http://localhost:8080/api/medical-records/1`

If the login is missing or wrong, the API answers **401 Unauthorized**. If you are logged in but your role is not allowed to do something, it answers **403 Forbidden** with a message explaining why.

## 2. What each role can do

### Receptionist
- Book, reschedule and cancel appointments. The system rejects overlapping appointments for the same vet or animal. (`/api/appointments`)
- Record the owner's consent for a surgery: `POST /api/surgeries/{id}/consent` with `{"accepted": true}`
- Give out **over-the-counter** products (e.g. flea shampoo) during a visit. Prescription-only products are rejected.
- [TODO Person 4: record payments, sell OTC products on an invoice]
- Cannot write medical records, perform treatments or change costs.

### General vet
Everything a vet needs during a visit, except surgery:
- Open a medical record for a visit: `POST /api/medical-records` with the animal and its weight
- View a record or an animal's full history: `GET /api/medical-records/{id}`, `GET /api/medical-records?animalId=PET-0001`
- Give a vaccine: `POST /api/medical-records/{id}/vaccines`. This automatically moves the animal's vaccination schedule to the next due date.
- Prescribe medication: `POST /api/medical-records/{id}/medications`. The dose is calculated from the weight recorded at the visit (dose per kg × weight).
- Correct the cost of a treatment: `PUT /api/treatments/{id}/cost`. The change is recorded in the audit log.
- Cannot plan or perform surgery.

### Surgeon
Everything a general vet can do, plus surgery:
- Plan a surgery: `POST /api/medical-records/{id}/surgeries` with the procedure and estimated cost
- Complete it: `POST /api/surgeries/{id}/complete` with the final cost and a date for the follow-up. This only works once the owner's consent has been signed. Completing it automatically books the follow-up appointment with the same surgeon.
- Cancel a planned surgery: `POST /api/surgeries/{id}/cancel`

### Clinic manager
- View the audit log, i.e. who changed which cost or surgery status, when, and from what to what:
    - All changes, newest first: `GET /api/audit`
    - History of one record: `GET /api/audit/Surgery/1`
- [TODO Person 4: dashboard and monthly report]
- [TODO Person 1: low-stock alerts]

## 3. Common errors
| Status | Meaning | Example |
|---|---|---|
| 400 | A rule or field is invalid | Completing a surgery without consent; negative weight |
| 401 | Not logged in, or wrong password | Missing `-u email:password` |
| 403 | Your role cannot do this | A general vet planning a surgery |
| 404 | The record does not exist | `GET /api/medical-records/999` |
| 409 | Conflict with the current state | Overlapping appointment; completing a cancelled surgery |

## 4. [TODO Person 2: vaccination schedules and reminders]
## 5. [TODO Person 1: clients, animals and inventory]
## 6. [TODO Person 4: invoices, payments and reports]