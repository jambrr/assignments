const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const redis = require('redis');
const fs = require('fs');
const csv = require('csv-parser');
const csvtojson = require('csvtojson');
const https = require('https');
app.use(bodyParser.json());
let results;

const client = redis.createClient({host: 'redis'});

app.get('/:country', (req, res) => {
	let country = req.params.country;
    //client.flushdb( function (err, succeeded) {
    //    console.log(succeeded); // will be true if successfull
    //});

	    client.get(country, function(err, rep){
            console.log(rep);
        if(rep != null){
            let object = JSON.parse(rep);
            console.log(object);
            res.send(object);
        }else{
            const file = async function(){fs.createWriteStream('data.csv');}
	        const request = https.get("https://covid19.who.int/WHO-COVID-19-global-table-data.csv", function(response){
	        	response.pipe(file);	
	        });

            file().then(function(){
                csvtojson().fromFile('data.csv')
                .then(data => {
                    for(var x in data){
                        if(data[x].Name === country){

                            let name = data[x]["Name"];
                            let cases_7_days = data[x]["Cases - newly reported in last 7 days"];
                            let cases_24_hrs = data[x]["Cases - newly reported in last 24 hours"];

                            let object = {
                                "country": name,
                                "date": new Date(),
                                "cases_7_days": cases_7_days,
                                "cases_24_hrs": cases_24_hrs
                            };
                            
                            results = object;
                            client.set(country, JSON.stringify(object));
                            res.send(object);
                        }
                    }
                }).catch(err => {
                    console.log(err);
                });
            });
        }
    });

});

var server = app.listen(8080, "nodejs",  () => {
  let host = server.address().address
  let port = server.address().port
  console.log("Example app listening at http://%s:%s", host, port)
})

