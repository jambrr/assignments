const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const redis = require('redis');
const fs = require('fs');
const csv = require('csv-parser');
const https = require('https');
app.use(bodyParser.json());
const results = [];

const client = redis.createClient({host: 'redis'});

app.get('/:country', (req, res) => {
	let country = req.params.country;

	//const file = fs.createWriteStream('data.csv');
	//const request = https.get("https://covid19.who.int/WHO-COVID-19-global-table-data.csv", function(response){
	//	response.pipe(file);	
	//});

    let json;

	fs.createReadStream('data.csv')
		.pipe(csv({delimiter: ','}))
		.on('data', (data) => results.push(data))
		.on('end', () => {
            //var rta = results.find((it) => { return it.Name == country;});
		});

        console.log(results[0].name);
        res.send("test");
});

var server = app.listen(8080, "nodejs",  () => {
  let host = server.address().address
  let port = server.address().port
  console.log("Example app listening at http://%s:%s", host, port)
})

