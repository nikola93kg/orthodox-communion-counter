# Бројач причесника (Црква Свете Тројице) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Израда комплетне, брзе, нативне Android апликације на српској ћирилици за таблет Samsung Galaxy Note 10.1 (LineageOS 16 / Android 9.0) намењене дискретном бројању причесника на Светој Литургији и вођењу црквеног Летописа причешћених.

**Architecture:** Чиста Android Java апликација (API 28) без непотребних спољних библиотечких зависности, са SQLite локалном базом података за трајно офлајн чување уноса, нечујном хаптичком вибрацијом, спречавањем гашења екрана (Keep Screen On) и прилагођеном византијском литургијском палетом са мотивом храма Свете Тројице.

**Tech Stack:** Java 8 / Android SDK Build-Tools 35.0.0 (`aapt2`, `javac`, `d8`, `zipalign`, `apksigner`), SQLite (`SQLiteOpenHelper`), Pillow (Python за припрему слика и иконица).

---

### File Structure Map

- `apps/communion-counter/`:
  - `AndroidManifest.xml`: Декларација дозволе за вибрацију (`VIBRATE`), активности `MainActivity` и `ChronicleActivity`.
  - `res/`:
    - `values/colors.xml`: Византијска бордо, злато, слоновача, тамни градијенти.
    - `values/strings.xml`: Сви текстови и поруке на српској ћирилици.
    - `values/styles.xml`: Литургијска тема без стандардног белог Android наслова, тамни дијалози.
    - `drawable/`: Позадински водени жиг цркве (`bg_church_watermark.png`), златни рамови (`border_gold.xml`), стил дугмади (`btn_gold.xml`, `btn_undo.xml`).
    - `mipmap/ic_launcher.png`: Иконица апликације (златни крст са црквом).
    - `layout/activity_main.xml`: Главни екран за бројање са огромном тач зоном.
    - `layout/activity_chronicle.xml`: Екран црквеног Летописа са статистиком и листом.
    - `layout/item_liturgy_record.xml`: Изглед појединачног записа у листи Летописа.
    - `layout/dialog_save_liturgy.xml`: Дијалог за упис Литургије и празника.
  - `src/org/orthodox/communioncounter/`:
    - `model/LiturgyRecord.java`: Модел класа за податке једне Литургије.
    - `db/LiturgyDbHelper.java`: SQLite база са CRUD операцијама и статистиком.
    - `MainActivity.java`: Главна активност бројача (додир, вибрација, ресет, дијалог уписа).
    - `ChronicleActivity.java`: Активност прегледа Летописа.
    - `LiturgyAdapter.java`: ListView адаптер за приказ записа.
  - `scripts/`:
    - `prepare_assets.py`: Python скрипта за генерисање оптимизованих слика из корисничког фајла.
    - `build_and_deploy.sh`: Скрипта за компајлирање (`aapt2` -> `javac` -> `d8` -> `zipalign` -> `apksigner` -> `adb install`).

---

### Task 1: Project Setup and Liturgical Assets Preparation

**Files:**
- Create: `apps/communion-counter/scripts/prepare_assets.py`
- Create: `apps/communion-counter/AndroidManifest.xml`
- Create: `apps/communion-counter/res/values/colors.xml`
- Create: `apps/communion-counter/res/values/strings.xml`
- Create: `apps/communion-counter/res/values/styles.xml`
- Create: `apps/communion-counter/res/drawable/border_gold.xml`
- Create: `apps/communion-counter/res/drawable/btn_gold.xml`
- Create: `apps/communion-counter/res/drawable/btn_undo.xml`

- [ ] **Step 1: Write asset preparation Python script**
  Узети слику цркве из `/Users/apple/.gemini/antigravity/brain/6f0c231a-6026-4265-9ef2-4a2bcb8e9a27/.user_uploaded/media_1792852628556_196e6788.webp`, генерисати:
  1. `bg_church_watermark.png`: затамњена/монохромна са 15% провидности преко тамне бордо подлоге `#1C0609`.
  2. `ic_launcher.png`: иконица православног крста и храма са златним обручом (размере 192x192, 144x144, 96x96, 72x72).

- [ ] **Step 2: Run asset script and verify drawables generated**
  Покренути `python3 apps/communion-counter/scripts/prepare_assets.py` и проверити излазне слике.

- [ ] **Step 3: Create Android resources (`colors.xml`, `strings.xml`, `styles.xml`, drawable XMLs)**
  Дефинисати комплетне српске ћириличне стрингове, боје византијског стила и облике дугмади са златним рамовима.

- [ ] **Step 4: Create AndroidManifest.xml**
  Дефинисати дозволу `android.permission.VIBRATE`, минимални SDK 21, циљни SDK 28, тему `@style/LiturgicalTheme`.

- [ ] **Step 5: Commit Task 1**
  `git add apps/communion-counter && git commit -m "feat: setup project structure, liturgical resources and assets"`

---

### Task 2: Database Layer (`LiturgyRecord` and `LiturgyDbHelper`)

**Files:**
- Create: `apps/communion-counter/src/org/orthodox/communioncounter/model/LiturgyRecord.java`
- Create: `apps/communion-counter/src/org/orthodox/communioncounter/db/LiturgyDbHelper.java`

- [ ] **Step 1: Write `LiturgyRecord.java`**
  Креирати модел са пољима `id`, `timestamp`, `dateFormatted`, `dayName`, `feastName`, `count`, `notes`.

- [ ] **Step 2: Write `LiturgyDbHelper.java`**
  Креирати SQLite базу `orthodox_records.db` са:
  - `onCreate`: табела `liturgy_records`
  - `insertRecord(LiturgyRecord record)`
  - `List<LiturgyRecord> getAllRecords()`
  - `deleteRecord(long id)`
  - `SummaryStats getSummaryStats()`: укупан број причешћених лица, укупно одслужених Литургија, просечан број.

- [ ] **Step 3: Verification**
  Компајлирати класе и покренути локалну верфикацију схеме.

- [ ] **Step 4: Commit Task 2**
  `git add apps/communion-counter/src && git commit -m "feat: add SQLite database helper and data models"`

---

### Task 3: Main Counter Activity (`MainActivity` & Layout)

**Files:**
- Create: `apps/communion-counter/res/layout/activity_main.xml`
- Create: `apps/communion-counter/src/org/orthodox/communioncounter/MainActivity.java`

- [ ] **Step 1: Create `activity_main.xml`**
  - Заглавље: Натпис *„Црква Свете Тројице“*, тренутни датум, дугме књиге за Летопис.
  - Централно поље: Велика тач површина, водени жиг цркве, огроман златни број и наслов *„Причесника“*.
  - Доња трака: Дугме `[- 1]`, главно дугме `«Заврши и упиши»` и дугме `«Поништи»`.

- [ ] **Step 2: Implement `MainActivity.java`**
  - Додаје `FLAG_KEEP_SCREEN_ON` да се екран никада не угаси током службе.
  - Притисак на екран: `counter++`, позива `vibrator.vibrate(VibrationEffect.createOneShot(35, 100))`.
  - Притисак на `-1`: ако је `counter > 0`, смањује за 1 и вибрира.
  - Притисак на „Поништи“: приказује `AlertDialog` са потврдом на српској ћирилици.
  - Чува тренутно стање у `SharedPreferences` тако да се број не изгуби чак ни ако се апликација случајно затвори.

- [ ] **Step 3: Commit Task 3**
  `git add apps/communion-counter && git commit -m "feat: implement main liturgical counter screen with haptics"`

---

### Task 4: Church Chronicle Register (`ChronicleActivity`, Save Dialog, Adapter)

**Files:**
- Create: `apps/communion-counter/res/layout/dialog_save_liturgy.xml`
- Create: `apps/communion-counter/res/layout/activity_chronicle.xml`
- Create: `apps/communion-counter/res/layout/item_liturgy_record.xml`
- Create: `apps/communion-counter/src/org/orthodox/communioncounter/LiturgyAdapter.java`
- Create: `apps/communion-counter/src/org/orthodox/communioncounter/ChronicleActivity.java`

- [ ] **Step 1: Create `dialog_save_liturgy.xml`**
  Дијалог са унапред попуњеним датумом, бројем причесника, брзим избором празника (радио дугмад или тастери) и пољем за белешку свештеника/појаца.

- [ ] **Step 2: Create `item_liturgy_record.xml` & `activity_chronicle.xml`**
  Картица са златном ивицом: датум, назив службе, златни беџ са бројем причешћених и белешка.
  Екран са збирном статистиком (Укупно причешћених, број Литургија, просек).

- [ ] **Step 3: Implement `LiturgyAdapter.java` and `ChronicleActivity.java`**
  Учитавање из базе, приказ у листи, дуг клик за брисање уноса са питањем за потврду.

- [ ] **Step 4: Commit Task 4**
  `git add apps/communion-counter && git commit -m "feat: implement church chronicle record book and statistics"`

---

### Task 5: Build, Package, Deploy to Tablet, and Verify

**Files:**
- Create: `apps/communion-counter/scripts/build_and_deploy.sh`

- [ ] **Step 1: Write automated build script**
  Скрипта користи:
  1. `aapt2 compile` и `aapt2 link` са `android-33/android.jar`.
  2. `javac` за компајлирање свих Java изворних кодова у bytecode.
  3. `d8` за претварање у Dalvik `classes.dex` (компатибилан са Android 9).
  4. Паковање у незапорављени APK помоћу `zip`.
  5. `zipalign -p 4`.
  6. `apksigner sign` са debug кључем.
  7. `adb install -r` директно на Galaxy Note 10.1.

- [ ] **Step 2: Execute build script and produce signed APK**
  Покренути израду пакета и верификовати да нема грешака у компајлирању.

- [ ] **Step 3: Deploy to tablet via ADB and launch**
  Инсталирати на таблет и покренути команду за отварање апликације.

- [ ] **Step 4: Final verification on device**
  Верификовати рад додира, вибрације, чувања у Летопис и изглед литургијске теме са сликом цркве Свете Тројице.

- [ ] **Step 5: Commit Task 5 and documentation**
  `git commit -am "chore: automated build script and production APK deployment"`
