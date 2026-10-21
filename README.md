# Barangay Assistance System

A Java Swing desktop application implementing the provided UML class diagram:
User hierarchy (Resident / Admin / Staff -> MedicalStaff, FoodStaff,
TransportationStaff), the AssistanceRequest hierarchy (MedicalRequest,
FoodRequest, TransportationRequest), and a FileManager that persists
everything to local text files.

## Project Structure

```
PF101-project/
├── bin/                       # Compiled .class files (git-ignored)
├── data/                      # Persistent storage (users.txt, requests.txt, etc.)
├── src/
│   ├── Main.java              # Application entry point
│   ├── model/                 # Domain entity classes
│   │   ├── User.java
│   │   ├── Resident.java
│   │   ├── Admin.java
│   │   ├── Staff.java
│   │   ├── MedicalStaff.java
│   │   ├── FoodStaff.java
│   │   ├── TransportationStaff.java
│   │   ├── AssistanceRequest.java
│   │   ├── MedicalRequest.java
│   │   ├── FoodRequest.java
│   │   ├── TransportationRequest.java
│   │   ├── FoodItem.java
│   │   ├── MedicalItem.java
│   │   ├── ResidentRequestHistory.java
│   │   └── StaffRequestHistory.java
│   ├── storage/               # Data persistence and managers (package storage)
│   │   ├── FileManager.java        # Reads/writes root data/*.txt
│   │   └── RequestManager.java
│   └── ui/                    # Graphical user interface frames
│       ├── LoginFrame.java         # Login (admin creates all accounts)
│       ├── ResidentMenuFrame.java
│       ├── StaffMenuFrame.java
│       └── AdminMenuFrame.java     # Includes Create User dialog
└── README.md
```
javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp bin Main
