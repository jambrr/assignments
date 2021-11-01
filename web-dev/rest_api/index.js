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
        Population: 1
    })

    res.send('asdf');
});

app.get('/read/:city', function(req, res){
    res.send('asdf');
});

app.get('/update/:city', function(req, res){
    res.send('asdf');
});

app.get('/delete/:city', function(req, res){
    res.send('asdf');
});


app.listen(3000, function(){
    console.log("service is running");
});