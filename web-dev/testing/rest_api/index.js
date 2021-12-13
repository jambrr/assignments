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

//Returns the row of the city we want
async function getRecord(city){
    let result = knex('city').where('Name', city).then(function(data){
        return data;
    });
    return result;
};

//Creates an ID based on the last record ID
async function createID(){
    let newID = knex('city').orderBy('ID', 'desc').then(function(total){
        return (total[0].ID + 1);
    });
    return newID;
}

//For creating data
app.post('/create', function(req, res){
    let city = req.body.city;
    let countryCode = req.body.countryCode;
    let district = req.body.district;
    let population = req.body.population;

    createID().then(function(newID){
        knex('city').insert({
            ID: newID,
            Name: city,
            CountryCode: countryCode,
            District: district,
            Population: population
        }).then(function(response){
            getRecord(city).then(function(record){
                console.log(record);
                res.send(record);
            });
        });
    });
});

//For reading data
app.get('/read/:city', function(req, res){
    let city = req.params.city;

    getRecord(city).then(function(r){
        res.send(r);
    });
});

//For updating the data
app.get('/updatePopulation/:city/:population', function(req, res){
    let city = req.params.city;
    let population = req.params.population;

    knex('city').update({Population: population}).where('Name', city).then(function(data){
        getRecord(city).then(function(r){
            res.send(r);
        });
    });
});

//For removing the data
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
