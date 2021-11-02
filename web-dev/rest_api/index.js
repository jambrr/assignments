const express = require('express');
const app = express();
app.use(express.json());

const knex = require('knex')({
    client: 'mysql2',
    connection: {
        host: '127.0.0.1',
        port: 3306,
        user: 'root',
        password: '',
        database: 'rest_api'
    },
});

async function getRecord(city){
    let result = knex('city').where('Name', city).then(function(data){
        return data;
    });
    return result;
};

app.post('/create', function(req, res){
    let city = req.body.city;
    let countryCode = req.body.countryCode;
    let district = req.body.district;
    let population = req.body.population;

    console.log(city, countryCode, district, population);

    knex('city').insert({
        Name: city,
        CountryCode: countryCode,
        District: district,
        Population: population
    }).then(function(res){
        console.log(res);
    });

    res.send('asdf');
});

app.get('/read/:city', async function(req, res){
    let city = req.params.city;

    getRecord(city).then(function(r){
        res.send(""+r[0].ID);
    });
});

app.get('/updatePopulation/:city/:population', function(req, res){
    let city = req.params.city;
    let population = req.params.population;

    knex('city').update({Population: population}).where('Name', city).then(function(data){
        getRecord(city).then(function(r){
            res.send(r);
        });
    });
});

app.get('/delete/:city', function(req, res){
    let city = req.params.city;
    
    knex('city').where('Name', city).del().then(function(data){
        let result = (data == 1)? true: false; 
        res.send(result);
    });
});


app.listen(3000, function(){
    console.log("service is running");
});
