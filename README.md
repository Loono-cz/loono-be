[![Build](https://github.com/cesko-digital/loono-be/actions/workflows/build_and_test_check.yml/badge.svg)](https://github.com/cesko-digital/loono-be/actions/workflows/build_and_test_check.yml)

<h1 align="center"><img src="https://www.loono.cz/files/logo-loono-colour-01.svg" alt="Logo" width="120"> Backend</h1>
<p align="justify">
The Loono backend covers all purposes for the Loono mobile application like user accounts, examinations or a doctor search.
The application provides REST API and a Swagger / Open API documentation.
</p>
<h2>Local development setup</h2>
<h3>Requirements</h3>
<ul>
<li>System environment variables</li>
<li>PostgreSQL database (version 12.5 or later)</li>
<li>JDK 17</li>
<li>IDE with git and gradle (or installed separately)</li>
<li>Service account credentials to the Firebase</li>
</ul>
<h3>Firebase Admin SDK</h3>
<p>
  We use Firebase fot the authentication. There is the Firebase Admin SDK on the server side. To initialize the client you need a system account for the mobile application. You have to download the service account credentials as a json file in the Admin console. 
More information <a href="https://firebase.google.com/docs/admin/setup#initialize-sdk">HERE</a>.
</p>
<h3>System environment variables</h3>
<ul>
<li>POSTGRE_URL: host:port/db (i.e. localhost:5432/loono</li>
<li>POSTGRE_USER: database owner</li>
<li>POSTGRE_PWD: database password</li>
<li>GOOGLE_APPLICATION_CREDENTIALS: a content of the file with service account credentials to the Firebase</li>
</ul>
<h3>Gradle plugins and tasks</h3>
<h4>Building bootable Jar file</h4>

```
gradlew build 
```
<p align="justify">
You can execute gradle build task to build the project and create bootable Jar file located as build/dists/loono-be.jar.
</p>
<h4>Running the server</h4>
The server is running at 8080 port by default.
<ul>
<li>Running in the IDE</li>
<p align="justify">
Execute the Application.kt as the main executable Class, debug supported.
</p>
<li>Running the built Jar</li>
<p align="justify">
The Jar file can be just executed.
</p>
</ul>
<h4>Jacoco</h4>

```
gradlew jacocoTestReport 
```
<p align="justify">
To check code coverage we support jacoco plugin. The report is located on build/reports/jacoco/test.
</p>
<h2>Heath check</h2>
<p>
The server uses Spring Actuator for the Health Check. The Health Check is hidden by basic authentication.
</p>
<h3>Basic health check endpoints</h3>
<p>
Activated only default and database checks. We can enable more if needed.
</p>
<ul>
<li>Overall status: /actuator/health</li>
<li>Database status: /actuator/health/db</li>
</ul>
<h2>API documentation</h2>
<ul>
<li>OpenAPI documentation on <i>/v3/api-docs</i></li>
</ul>
