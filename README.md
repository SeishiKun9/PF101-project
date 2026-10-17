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
│   ├── data/                  # Data persistence and managers
│   │   ├── FileManager.java
│   │   └── RequestManager.java
│   └── ui/                    # Graphical user interface frames & styling
│       ├── JButtonStyle.java
│       ├── LoginFrame.java
│       ├── RegisterFrame.java
│       ├── ResidentMenuFrame.java
│       ├── StaffMenuFrame.java
│       └── AdminMenuFrame.java
└── README.md
```

## How to compile and run

Requires JDK 11+.

### Option 1: Using javac into bin/ (Recommended)

From the `PF101-project` directory:

```bash
# Windows PowerShell
javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp bin Main

# Bash / Linux / macOS
javac -d bin $(find src -name "*.java")
java -cp bin Main
```

### Option 2: Run directly in VS Code / Antigravity IDE
Open `src/Main.java` and click **Run** or press `F5`.

## Design notes

- The model APIs follow the UML signatures, including parameterized resident
  request actions and admin user management. GUI dialogs and input validation
  remain in the matching `*MenuFrame` classes, which call those model APIs.
- `User.login()` completes authentication after `LoginFrame` verifies the
  supplied password through the helper `authenticate(String)` method.
- `displayDetails()` prints to console per the diagram; a
  `toDisplaySummary(): String` helper (overridden per subclass, i.e.
  polymorphism) feeds the same text into GUI tables/dialogs.
- Requests share location, needed time, additional details, status, and
  submission time. Food and medical requests store lists of typed items;
  transportation requests store pickup, destination, and passenger count.
- Data is stored as pipe-delimited (`|`) text under `data/users.txt` and
  `data/requests.txt` so it survives restarts. New request records escape
  delimiter characters, and the FileManager continues to read existing
  request records while saving new request structures.
