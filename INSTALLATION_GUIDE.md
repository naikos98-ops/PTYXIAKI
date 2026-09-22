# Οδηγός Εγκατάστασης & Ρύθμισης (Installation Guide) — EstateSol

Αυτός ο οδηγός περιέχει αναλυτικές οδηγίες για την εγκατάσταση, τη ρύθμιση και την εκτέλεση της πλατφόρμας **EstateSol** σε τοπικό περιβάλλον ανάπτυξης (Windows).

---

## 1. Προαπαιτούμενα Συστήματος

Πριν ξεκινήσετε, βεβαιωθείτε ότι έχετε εγκαταστήσει τα εξής:

1. **Node.js (Έκδοση 18+)** και **npm**: Απαραίτητα για το Vue.js 3 frontend.
   * [Λήψη Node.js](https://nodejs.org/)
2. **PostgreSQL (Έκδοση 15+)**: Η σχεσιακή βάση δεδομένων της εφαρμογής.
   * [Λήψη PostgreSQL](https://www.postgresql.org/)
3. **Google Cloud CLI (gcloud)**: Απαραίτητο για την πιστοποίηση (authentication) με τις υπηρεσίες Google Cloud / Vertex AI.
   * [Λήψη Google Cloud CLI](https://cloud.google.com/sdk/docs/install)

---

## 2. Αυτόματη & Χειροκίνητη Ρύθμιση Java 17

Το backend της εφαρμογής απαιτεί **Java 17**. Το project περιέχει έναν αυτοματοποιημένο μηχανισμό για τη λήψη και ρύθμισή της.

* **Αυτόματος Τρόπος:**
  Στον φάκελο `tools/` υπάρχει το αρχείο `openjdk.zip` (ή αν δεν υπάρχει, θα ληφθεί αυτόματα) και το PowerShell script [setup_java.ps1](file:///c:/Users/Naiko/Desktop/EstateSol/tools/setup_java.ps1). 
  Το κεντρικό script εκτέλεσης [run.ps1](file:///c:/Users/Naiko/Desktop/EstateSol/run.ps1) ελέγχει αν η Java είναι ρυθμισμένη και εκτελεί αυτόματα το setup.

* **Χειροκίνητος Τρόπος:**
  Αν επιθυμείτε να χρησιμοποιήσετε δική σας εγκατάσταση Java 17, βεβαιωθείτε ότι έχετε ορίσει τη μεταβλητή περιβάλλοντος `JAVA_HOME` να δείχνει στην εγκατάσταση της Java 17 και ότι η διαδρομή `%JAVA_HOME%\bin` έχει προστεθεί στο `PATH` του συστήματός σας.

---

## 3. Ρύθμιση Βάσης Δεδομένων (PostgreSQL)

Η εφαρμογή χρησιμοποιεί τη βάση δεδομένων PostgreSQL.

1. **Δημιουργία Βάσης:**
   Ανοίξτε το εργαλείο διαχείρισης της PostgreSQL (π.χ. pgAdmin ή `psql` μέσω terminal) και δημιουργήστε μια νέα βάση δεδομένων με το όνομα `performax` (το προεπιλεγμένο όνομα στο αρχείο ρυθμίσεων):
   ```sql
   CREATE DATABASE performax;
   ```

2. **Στοιχεία Σύνδεσης:**
   Η εφαρμογή αναζητά τις εξής προεπιλεγμένες τιμές στο [application.properties](file:///c:/Users/Naiko/Desktop/EstateSol/EstateSol/src/main/resources/application.properties):
   * **Host/Port:** `localhost:5432`
   * **Database:** `performax`
   * **Username:** `postgres`
   * **Password:** `1234`

   *Σημείωση: Αν τα δικά σας στοιχεία σύνδεσης διαφέρουν, μπορείτε να ορίσετε τις κατάλληλες μεταβλητές περιβάλλοντος στο σύστημά σας (ή να τις περάσετε κατά την εκτέλεση):*
   * `SPRING_DATASOURCE_URL` (π.χ. `jdbc:postgresql://localhost:5432/my_custom_db`)
   * `SPRING_DATASOURCE_USERNAME`
   * `SPRING_DATASOURCE_PASSWORD`

---

## 4. Ρύθμιση Πιστοποιητικών Google Cloud (Vertex AI & Imagen 3)

Το EstateSol χρησιμοποιεί μοντέλα παραγωγικής τεχνητής νοημοσύνης (**Gemini 2.5** και **Imagen 3**) μέσω της πλατφόρμας Vertex AI της Google Cloud. Για να λειτουργήσουν οι AI δυνατότητες:

1. **Ενεργοποίηση API:**
   Βεβαιωθείτε ότι στο Google Cloud Console του Project σας είναι ενεργοποιημένο το **Vertex AI API**.

2. **Πιστοποίηση (Authentication) μέσω Application Default Credentials (ADC):**
   Ανοίξτε το PowerShell/Command Prompt και εκτελέστε την παρακάτω εντολή για να συνδεθείτε με τον λογαριασμό σας GCP:
   ```sh
   gcloud auth application-default login
   ```
   Αυτό θα δημιουργήσει ένα τοπικό αρχείο πιστοποίησης, το οποίο το Spring Boot backend θα εντοπίσει αυτόματα για να κάνει τις κλήσεις στα API.

3. **Ορισμός Μεταβλητών GCP (Προαιρετικά αν διαφέρουν από τα defaults):**
   * `GOOGLE_CLOUD_PROJECT_ID` (Προεπιλογή: `estatesol`)
   * `GOOGLE_CLOUD_REGION` (Προεπιλογή: `europe-west4`)

---

## 5. Εκτέλεση της Εφαρμογής

Υπάρχουν δύο τρόποι εκτέλεσης της πλατφόρμας:

### Μέθοδος Α: Αυτόματη εκτέλεση με το `run.ps1` (Προτεινόμενη)

Το project διαθέτει ένα κεντρικό PowerShell script [run.ps1](file:///c:/Users/Naiko/Desktop/EstateSol/run.ps1) στη ρίζα του φακέλου.

1. Ανοίξτε το **PowerShell** με δικαιώματα εκτέλεσης scripts στη ρίζα του project:
   ```powershell
   .\run.ps1
   ```
2. Θα εμφανιστεί ένα μενού επιλογών:
   ```text
   ==========================================
              EstateSol Runner Script        
   ==========================================
   1. Start Backend (Spring Boot)
   2. Start Frontend (Vue.js Dev Server)
   3. Start BOTH (Backend and Frontend in separate windows)
   ==========================================
   Select an option (1-3):
   ```
3. Επιλέξτε `3` για να ξεκινήσουν **ταυτόχρονα** και οι δύο υπηρεσίες σε ξεχωριστά παράθυρα κονσόλας.

---

### Μέθοδος Β: Χειροκίνητη Εκτέλεση

Αν προτιμάτε να εκτελέσετε τις υπηρεσίες χειροκίνητα:

#### 1. Εκκίνηση Backend (Spring Boot)
1. Ανοίξτε ένα τερματικό και μεταβείτε στον φάκελο του backend:
   ```sh
   cd EstateSol
   ```
2. Εκτελέστε την εφαρμογή χρησιμοποιώντας το Maven Wrapper:
   ```sh
   .\mvnw.cmd spring-boot:run
   ```
   *Το backend θα ξεκινήσει στη θύρα **8082** (`http://localhost:8082`).*

#### 2. Εκκίνηση Frontend (Vue.js 3 + Vite)
1. Ανοίξτε ένα δεύτερο τερματικό και μεταβείτε στον φάκελο του frontend:
   ```sh
   cd VUE-JS
   ```
2. Εγκαταστήστε τις εξαρτήσεις (μόνο την πρώτη φορά ή μετά από ενημερώσεις):
   ```sh
   npm install
   ```
3. Εκκινήστε τον dev server:
   ```sh
   npm run dev
   ```
   *Το frontend θα ξεκινήσει στη θύρα **5173** (`http://localhost:5173`).*

---

## 6. Πρόσβαση στην Εφαρμογή

Μόλις εκτελεστούν επιτυχώς και τα δύο μέρη:
1. Ανοίξτε τον browser σας στη διεύθυνση: **[http://localhost:5173](http://localhost:5173)**
2. Ο dev server του Vite είναι ρυθμισμένος να δρομολογεί αυτόματα (proxy) όλα τα αιτήματα API (π.χ. `/api/*`, `/login`, `/register`) στη θύρα `8082` του backend, αποφεύγοντας προβλήματα CORS.
3. Κατά την εκκίνηση, το Hibernate θα δημιουργήσει/ενημερώσει αυτόματα τους πίνακες στη βάση δεδομένων `performax` και η κλάση `DbConstraintCleaner` θα εκτελέσει τυχόν απαραίτητα migrations.
