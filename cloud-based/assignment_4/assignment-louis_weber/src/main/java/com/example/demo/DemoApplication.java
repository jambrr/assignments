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

    @RequestMapping("/get")
    public String get() {
        DBObject query = new BasicDBObject("_id", "1");
        DBCursor cursor = dishes.find(query);

        return cursor.one().get("name").toString();
    }

    @RequestMapping("/order")
    public String order() {

        return "Order";
    }
    public static void main(String[] args){
        try{
            mongoClient = new MongoClient("mongo_db", 27017);
            database = mongoClient.getDB("restaurant");
            dishes = database.getCollection("dishes");
            orders = database.getCollection("orders");
            DBObject o = new BasicDBObject("_id", "1").append("name", "louis");
            dishes.insert(o);

        }catch(Exception e){
            System.out.println("host not found");
        }
        SpringApplication.run(DemoApplication.class, args);
    }
}
