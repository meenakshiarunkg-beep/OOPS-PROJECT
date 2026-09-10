# Online Voting System — Setup & Run Guide

## What you need installed
1. **Java JDK 17+** — https://adoptium.net (download, run installer, accept defaults)
2. **VS Code** — https://code.visualstudio.com
3. **"Extension Pack for Java"** (by Microsoft) — inside VS Code, click the Extensions icon (left sidebar) and search for it, click Install. This one extension pack includes everything needed to run Java in VS Code.
4. **XAMPP** (gives you MySQL) — https://www.apachefriends.org — install, then open the XAMPP Control Panel and click **Start** next to MySQL.
5. **MySQL Connector/J** (the JDBC driver `.jar`) — download from https://dev.mysql.com/downloads/connector/j/ (choose "Platform Independent", download the `.zip` or `.tar.gz`, and pull out the file named something like `mysql-connector-j-9.x.x.jar`).

## Step-by-step setup

### 1. Open the project folder
In VS Code: **File → Open Folder** → select this `OnlineVotingSystem` folder.

### 2. Add the JDBC driver to the project
- Create a folder named `lib` inside `OnlineVotingSystem` (next to `src`).
- Put the `mysql-connector-j-9.x.x.jar` file you downloaded into `lib`.
- In VS Code, open the Command Palette (`Ctrl+Shift+P` / `Cmd+Shift+P`), type **"Java: Configure Classpath"**, and add the jar from `lib` if it isn't picked up automatically. (Usually the Java extension auto-detects any `.jar` inside a `lib` folder — check the "Java Projects" panel in the sidebar to confirm it shows up under "Referenced Libraries".)

### 3. Set up the database
- Open **phpMyAdmin** (from the XAMPP Control Panel, click "Admin" next to MySQL, or go to `http://localhost/phpmyadmin`).
- Click **Import**, choose the file `src/sql/online_voting.sql`, click **Go**.
- This creates the `online_voting` database with all 4 tables, a default admin login, and 2 sample candidates.

### 4. Check your DB credentials match
Open `src/database/DBConnection.java`. The defaults are:
```
URL = jdbc:mysql://localhost:3306/online_voting
USERNAME = root
PASSWORD = (blank)
```
This matches a standard fresh XAMPP install. If your MySQL uses a different port, username, or password, edit these 3 lines.

### 5. Test the database connection first
Right-click `src/database/DBConnection.java` in VS Code → **Run Java**.
You should see `Database Connected Successfully` in the terminal.
If you see an error instead, fix it here before moving on — nothing else will work until this line succeeds.

### 6. Run the whole app
Right-click `src/Main.java` → **Run Java**.
The voter login window should appear.

## How to test the full flow
1. Click **Register as New Voter** → sign up with a name/username/password.
2. Click **Admin Login** (default: `admin` / `admin123`) → **View / Approve Voters** → approve the voter you just created.
3. Go back to voter login, log in with that voter's credentials.
4. On the Voter Dashboard, click **Cast Vote**, pick a candidate, submit.
5. Try to vote again with the same account — it should say "You have already voted."
6. From the Admin Dashboard, click **View Results** to see the tally and winner.

## Project structure
```
OnlineVotingSystem/
  lib/                      <- put the MySQL Connector/J jar here
  src/
    Main.java               <- run this
    database/DBConnection.java
    model/User.java, Voter.java, Admin.java, Candidate.java
    login/LoginFrame.java, RegistrationFrame.java
    admin/AdminLogin.java, AdminDashboard.java
    voter/VoterDashboard.java
    voting/VotingFrame.java, ResultsFrame.java
    sql/online_voting.sql
```

## OOP concepts you can point to
- **Encapsulation:** private fields + getters/setters in `Voter`, `Admin`, `Candidate`.
- **Inheritance:** `User` → `Voter` and `User` → `Admin`.
- **Abstraction:** every screen calls `DBConnection.getConnection()` without knowing how the connection is built.
- **Polymorphism:** `Voter` and `Admin` both extend `User` but represent different kinds of accounts with different data/behavior.
