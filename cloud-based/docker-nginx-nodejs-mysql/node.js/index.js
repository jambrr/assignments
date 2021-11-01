const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const redis = require('redis');
const fs = require('fs');
const csvtojson = require('csvtojson');
const https = require('https');
app.use(bodyParser.json());

const client = redis.createClient({host: 'redis'});

app.get('/:country', (req, res) => {
    console.log(__filename);
	let country = req.params.country;
    //client.flushdb( function (err, succeeded) {
    //   console.log(succeeded); // will be true if successfull
    //});

    client.get(country, function(err, rep){
        if(rep != null){
            console.log('Retrieve data from the database');
            let jsonObject = JSON.parse(rep);
            res.send(jsonObject);
        }else{
            console.log('Retrieve data from the website');
            //Download csv file
            const file = fs.createWriteStream(__dirname+'/covid_data2.csv');
            const httpsRequest = new Promise((resolve, reject) => {
                https.get('https://covid19.who.int/WHO-COVID-19-global-table-data.csv', function(response){
                  resolve(response.pipe(file));
              });
            });
            
            //Filter the csv file
            httpsRequest.then(value => {
                csvtojson().fromFile('covid_data.csv')
                .then(data => {
                    for(var i in data){
                        if(data[i].Name === country){

                            let name = data[i]['Name'];
                            let cases_7_days = data[i]['Cases - newly reported in last 7 days'];
                            let cases_24_hrs = data[i]['Cases - newly reported in last 24 hours'];

                            let date = new Date();

                            let jsonObject = {
                                'country': name,
                                'date': date.getFullYear()+'-'+(date.getMonth()+1)+'-'+date.getDate(),
                                'cases_7_days': cases_7_days,
                                'cases_24_hrs': cases_24_hrs
                            };
                            
                            results = jsonObject;
                            client.set(country, JSON.stringify(jsonObject));
                            res.send(jsonObject);
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

