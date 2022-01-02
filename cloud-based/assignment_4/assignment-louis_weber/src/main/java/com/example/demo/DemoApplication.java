package com.example.demo;

import com.mongodb.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.UnknownHostException;
import java.util.Date;
import java.net.InetAddress;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class DemoApplication {
    static MongoClient mongoClient;
    static DB database;
    static DBCollection dishes;
    static DBCollection orders;

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot, Java version!";
    }

    @RequestMapping("/get/{dishName}")
    public String get(@PathVariable String dishName) {
        System.out.println(dishName);
        DBObject query = new BasicDBObject("_name", dishName);
            DBCursor cursor = dishes.find(query);

        return cursor.one().toString();
    }

    @RequestMapping("/order/{dishName}/{address}")
    public String order(@PathVariable String dishName, @PathVariable String address) {
        int rand = (int)(Math.random() * 1000);
        
        DBObject order = new BasicDBObject("_id", rand)
            .append("name", dishName)
            .append("address", address);

        orders.insert(order);
        return "Order of: "+dishName+" has been made and will be delivered to: "+address;
    }

    @RequestMapping("/add/{dishName}/{description}/{category}/{price}")
    public String add(@PathVariable String dishName, @PathVariable String description, @PathVariable String category, @PathVariable double price) {

        DBObject dish = new BasicDBObject("_name", dishName)
            .append("description", description)
            .append("category", category)
            .append("price", price);

        dishes.insert(dish);
        return "New dish has been added";
    }

    public static void main(String[] args){
        try{
            mongoClient = new MongoClient("mongo_db", 27017);
            database = mongoClient.getDB("restaurant");
            dishes = database.getCollection("dishes");
            orders = database.getCollection("orders");

        }catch(Exception e){
            System.out.println("host not found");
        }
        SpringApplication.run(DemoApplication.class, args);
    }
}
