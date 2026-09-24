# Online Voting System (Java + Swing + MySQL)

A complete desktop election app: voters register, log in, and cast one
vote each; admins add/remove candidates, manage voters, and watch live
results. Built in plain Java (no frameworks needed beyond the MySQL
driver) so it opens directly in **VS Code**.

---

## 1. Features

**Voter side**
- Register with Voter ID, email, date of birth, password, and a security
  question (used later for password recovery).
- Log in with Voter ID *or* email + password.
- **Forgot password** — a real 4-step verification wizard:
  1. Enter Voter ID/email
  2. Confirm date of birth on file
  3. Answer your security question
  4. Set a new password
  Three wrong answers across steps 2–3 locks the account (same as failed
  logins) until an admin unlocks it.
- Browse candidates (photo or auto-generated initials avatar, name,
  party, position) and cast exactly one vote, with a confirmation dialog.
- Can't vote twice, even by clicking fast or opening the app twice
  (enforced with a database transaction + row lock, not just a UI check).
- Sees a "voting closed" screen if the admin has closed the election.

**Admin side**
- Separate Admin Login tab (default `admin` / `Admin@123`, created
  automatically the first time the app runs against an empty database).
- **Add / Edit / Remove candidates**, with an optional photo picker.
  Removing a candidate that already has votes shows a clear warning
  before it lets you continue.
- **Manage voters**: see everyone's status (voted / not voted, locked /
  not locked), unlock an account that got locked out, or delete a voter.
- **Live results** as a hand-drawn bar chart (votes + percentages),
  visible only to the admin — regular voters never see the running
  tally, only the admin does (a normal fairness rule for elections).
- **Open / Close voting** with one click, and edit the election title.
- Change the admin password from inside the app.

**Security basics covered**
- Every password (voter + admin) and every security-question answer is
  stored **salted + hashed** (SHA-256 + a random salt per user) — never
  in plain text.
- All database queries use `PreparedStatement` (no SQL injection).
- Vote casting is wrapped in a real DB transaction with row locking, so
  double-voting can't happen even under a race condition.
- Account lockout after repeated failed logins or failed identity checks
  during password recovery.

---

## 2. What you need installed

1. **JDK 17 or newer** — [https://adoptium.net](https://adoptium.net)
2. **MySQL Server** (Community Edition is fine) —
   [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/)
   During install, remember the **root password** you set — you'll need it below.
3. **VS Code** with the **"Extension Pack for Java"** (by Microsoft) installed
   from the Extensions panel.
4. **MySQL Connector/J** (the JDBC driver jar) — download the "Platform
   Independent" ZIP/jar from
   [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)
   and copy the file named something like `mysql-connector-j-9.x.x.jar`
   into this project's `lib/` folder. **This step is required** — the app
   cannot talk to MySQL without it, and it isn't bundled in this download
   because it's a separate Oracle license.

---

## 3. Set up the database

1. Open **MySQL Workbench** (or the `mysql` command line) and connect as `root`.
2. Open the file `sql/voting_system.sql` from this project and run the
   whole script. It creates the `voting_system` database and all tables.
   (It's safe to re-run — it drops and recreates the database each time,
   which is handy if you want a clean slate while testing.)

---

## 4. Point the app at your database

Open `src/com/voting/db/DBConnection.java` and edit these two lines near
the top to match your own MySQL username/password:

```java
private static final String USER = "root";      // <-- your MySQL username
private static final String PASSWORD = "root";  // <-- your MySQL password
```

If MySQL is running on a different machine or port, also adjust the `URL`
constant above it.

---

## 5. Open and run it in VS Code

1. **File → Open Folder...** and choose this `VotingSystem` folder.
2. Wait a few seconds for the Java extension to index the project (you'll
   see "Loading Java Projects..." in the bottom status bar).
3. Make sure `mysql-connector-j-*.jar` is inside `lib/` (step 2 above) —
   VS Code is already configured (see `.vscode/settings.json`) to treat
   everything in `lib/` as a library automatically.
4. Open `src/com/voting/ui/MainApp.java`, and click the **▶ Run** arrow
   that appears above `public static void main`. (Or press **F5** — a
   launch config for this is already set up in `.vscode/launch.json`.)

The first time it runs against a fresh database, it prints
`Default admin created -> username: admin | password: Admin@123` to the
Debug Console and you can log in with that on the **Admin Login** tab.

### Command-line alternative (no VS Code)

```bash
# macOS / Linux
./compile.sh
./run.sh

# Windows
compile.bat
run.bat
```

### A note on BlueJ

BlueJ can technically run this, but it's built for single-class or
single-package teaching projects, and this one spans five packages plus
an external JDBC jar — adding that jar to BlueJ's classpath (via
*Preferences → Libraries*) is fiddly and BlueJ's package-at-a-time model
makes multi-package Swing apps awkward to launch correctly. **VS Code is
the recommended way to run this project.** If your course specifically
requires BlueJ, say so and this can be restructured into fewer
BlueJ-friendly classes.

---

## 6. Project structure

```
VotingSystem/
├── sql/voting_system.sql        <- run this once in MySQL
├── lib/                         <- put mysql-connector-j-*.jar here
├── candidate_photos/            <- uploaded candidate photos land here
├── src/com/voting/
│   ├── db/DBConnection.java     <- JDBC connection (edit your credentials here)
│   ├── model/                   <- Voter, Candidate, AdminUser (plain data classes)
│   ├── dao/                     <- all SQL lives here (one class per table)
│   ├── util/                    <- password hashing + input validation
│   └── ui/                      <- every screen (Swing panels) + MainApp entry point
├── compile.bat / run.bat        <- Windows command-line build
└── compile.sh / run.sh          <- macOS/Linux command-line build
```

Each screen (`LoginPanel`, `RegisterPanel`, `ForgotPasswordPanel`,
`AdminDashboardPanel`, `VoterDashboardPanel`, and the three admin tabs)
is its own file, all under `ui/`, so it's easy to find and change any one
screen without touching the others.

---

## 7. Things worth knowing / extending later

- **One vote per election, not per position.** Candidates have a
  "Position" label for organization, but the schema tracks a single
  `has_voted` flag per voter — this fits a single-seat election (e.g.
  "Student Council President"). For multiple independent positions
  (President + Treasurer, each voted separately), the `votes`/`voters`
  tables would need a per-position voted flag — ask if you want that
  extended.
- **Password recovery uses a security question**, not email, since that
  needs no external mail server to set up and demo. If you want real
  email OTP recovery instead/also, that's a clean addition on top of
  `ForgotPasswordPanel` using the `javax.mail` (JavaMail) library plus a
  Gmail app password.
- Photos are copied into `candidate_photos/` next to the app rather than
  stored in the database, which keeps the database small and easy to
  inspect directly in MySQL Workbench.
