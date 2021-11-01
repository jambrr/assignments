# REST Example with Node.js #

The JS code is inside the file _./node.js/index.js_. Note that a build of the Docker application will update _npm_ and _node.js_ to latest versions, then use _npm_ to download all required JS dependencies.

For size reasons, all administrative and data files for the mysql DB have been removed from the _./mysql_ directory. Therefore, to use the example webservice, you must first load the data into the database (after having started the application) before accessing the access URL:

* _mysql -h 127.0.0.1 -u webprog -p webprog < DB.dump_ (with password as given in the Dockerfile)
* Access _http://localhost/listUsers_ to see all users in the database table in JSON format
