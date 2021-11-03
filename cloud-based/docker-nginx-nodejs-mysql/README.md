# REST Example with Node.js #

The JS code is inside the file _./node.js/index.js_. Note that a build of the Docker application will update _npm_ and _node.js_ to latest versions, then use _npm_ to download all required JS dependencies.

For size reasons, all administrative and data files for the mysql DB have been removed from the _./mysql_ directory. Therefore, to use the example webservice, you must first load the data into the database (after having started the application) before accessing the access URL:

* _mysql -h 127.0.0.1 -u webprog -p webprog < DB.dump_ (with password as given in the Dockerfile)
* Access _http://localhost/listUsers_ to see all users in the database table in JSON format

Description:
Author: Louis James Weber(019087433A)

The code works by first checking wether the country already exists in the redis database by looking if there exists a key with the country name. If not, we will download the csv file from the website(given in the assignment description) and store it in the node.js directory, we then read the csv file to get the data of the country we want. We create a json object with the data we want, we stringify the object and store it in the database.
If however the data already exists in the database, we retrieve it by using the country parameter from the url which also acts as a key for the database, and the result we get is a json string which we parse into a json object and then print unto the webpage.

For example: http://172.18.0.3:8080/Germany will return all the information of the country Germany
