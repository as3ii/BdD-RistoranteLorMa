# BdD - RistoranteLorMa
Project for the course "Basi di Dati" 2024-25

## Running the application

### Requirements
- JDK version 21 or later
- MariaDB 11 or later

### Running the application
- Start the MySQL server (MariaDB) and setup the database (create new database using `tables.sql`, or use the provided docker `compose.yaml`)
- NOTE: On windows, any `java` or `gradle` call needs to be followed by the parameter `-Dfile.encoding=UTF-8`
- NOTE: If you need to set the database name or its server address, user and password, you can use the following environment variables:
    - `DB_HOSTNAME`: server hostname/IP (default: localhost)
    - `DB_PORT`: server port (default: 3306)
    - `DB_NAME`: database name (default: APP_RISTORANTI)
    - `DB_USER`: database user (default: root)
    - `DB_PASSWORD`: user's password (default empty)
- On linux, if you want 2x integer scaling for the UI, set the environment variable `GDK_SCALE=2`
- Launch the application following one of this options:
    - launch `gradle run` inside this folder
    - download from the release section the .jar bundle file and execute it running `java -jar RistoranteLorMa-all.jar`,
      or build your own jar file with `gradle shadowJar` and then look inside `build/libs`
