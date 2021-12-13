const express = require('express');
const app = express();
app.use(express.json());

const knex = require('knex')({
    client: 'mysql2',
    connection: {
        host: 'mysql-app',
        port: 3306,
        user: 'root',
        password: 'test',
        database: 'webdev'
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

/**
 * @api {post} /create Request to add a city
 * @apiName CreateCity
 * @apiGroup City
 * @apiParam {String} city          Name of the city
 * @apiParam {String} countryCode   Country code of the city
 * @apiParam {String} district      District of the city
 * @apiParam {Number} population    Population of the city
 *
 * @apiSuccess {String} city          Name of the city
 * @apiSuccess {String} countyCode    Country code of the city
 * @apiSuccess {String} district      District of the city
 * @apiSuccess {Number} population    Population of the city
 *
 * @apiSuccessExample {json} Succes-Response:
 *  HTTP/1.1 200 OK
 *  {
 *      "city": "Luxembourg",
 *      "countryCode": "LU",
 *      "district": "Luxembourg",
 *      "population": "100000"
 *  }
 */
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

/**
 * @api {get} /read/:city Request city information
 * @apiName GetCity
 * @apiGroup City
 * @apiParam {String} city Name of the city
 * @apiSuccess {String} city          Name of the city
 * @apiSuccess {String} countyCode    Country code of the city
 * @apiSuccess {String} district      District of the city
 * @apiSuccess {Number} population    Population of the city
 *
 * @apiSuccessExample {json} Succes-Response:
 *  HTTP/1.1 200 OK
 *  {
 *      "city": "Luxembourg",
 *      "countryCode": "LU",
 *      "district": "Luxembourg",
 *      "population": "100000"
 *  }

 */
app.get('/read/:city', function(req, res){
    let city = req.params.city;

    getRecord(city).then(function(r){
        res.send(r);
    });
});

/**
 * @api {get} /updatePopulation/:city/:population Request to update the population of a city
 * @apiName UpdateCityPopulation
 * @apiGroup City
 * @apiParam {String} city Name of the city
 * @apiParam {Number} population of the city
 *
 * @apiSuccess {String} city          Name of the city
 * @apiSuccess {String} countyCode    Country code of the city
 * @apiSuccess {String} district      District of the city
 * @apiSuccess {Number} population    Population of the city
 *
 * @apiSuccessExample {json} Succes-Response:
 *  HTTP/1.1 200 OK
 *  {
 *      "city": "Luxembourg",
 *      "countryCode": "LU",
 *      "district": "Luxembourg",
 *      "population": "100000"
 *  }
 */
app.get('/updatePopulation/:city/:population', function(req, res){
    let city = req.params.city;
    let population = req.params.population;

    knex('city').update({Population: population}).where('Name', city).then(function(data){
        getRecord(city).then(function(r){
            res.send(r);
        });
    });
});

/**
 * @api {get} /delete/:city Request to delete a city
 * @apiName DeleteCity
 * @apiGroup City
 * @apiParam {String} city Name of the city
 *
 * @apiSuccess {Number} Integer 1, inticating that the deletion was successful
 *
 * @apiSuccessExample {json} Succes-Response:
 *  HTTP/1.1 200 OK
 *  {
 *      1
 *  }
 *
 * @apiError {Number} Integer 0, inticating that the deletion failed
 * @apiErrorExample {json} Error-Response:
 *  HTTP/1.1 200 OK
 *  {
 *      0
 *  }
 *

 */
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
