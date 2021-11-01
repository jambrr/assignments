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
    //   console.log(succeeded); // will be true if successfull
    //});

    client.keys('*', function(err, keys){
        for(var x in keys){
            //console.log(x, keys[x]);
        }
    });

    client.get(country, function(err, rep){
        console.log(rep);
        if(rep != null){
            console.log("Retrieve data from the database");
            let object = JSON.parse(rep);
            res.send(object);
        }else{
            console.log("Retrieve data from the website");
            //Download csv file
            const file = fs.createWriteStream('data.csv');
            const request = new Promise((resolve, reject) => {
                https.get("https://covid19.who.int/WHO-COVID-19-global-table-data.csv", function(response){
                  resolve(response.pipe(file));
              });
            });
            
            //Filter the csv file
            request.then(value => {
                csvtojson().fromFile('data.csv')
                .then(data => {
                    for(var x in data){
                        if(data[x].Name === country){

                            let name = data[x]["Name"];
                            let cases_7_days = data[x]["Cases - newly reported in last 7 days"];
                            let cases_24_hrs = data[x]["Cases - newly reported in last 24 hours"];

                            let date = new Date();

                            let object = {
                                "country": name,
                                "date": date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate(),
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

