# REST WebService with SpringBoot and Java #

This project includes two different variants on providing a simple
RESTful WebService Provider with SpringBoot and Java:

* SpringBoot, Java, and Maven
* SpringBoot, Java, and Gradle

Note that the example runs only with a running MySQL DB. You can run this database with the command _docker-compose build; docker-compose up -d_. The database should already be pre-loaded with some data; if not, you can upload the provided DB dump given in the file _SpringBoot-REST-DB.sql_. Consistent with the MySQL setup, the SpringBoot configuration file given in  _/src/main/resources/application.properties_ uses the following DB-related information:

* **host:** localhost
* **user:** javaee
* **password:** eeavaj

# Running the examples: #

* Java example, mvn version: _mvn spring-boot:run_
* Java example, Gradle (7.0.x) version: _gradle bootRun_