package lu.uni.javaee.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
public class HelloController {
  @Autowired private StudentRepository sr;

  @RequestMapping("/")
  public String index() {
    return "Greetings from Spring Boot, Java version!";
  }

  @RequestMapping(value={"/student","/student/{id}"})
  public Student findStudent(@PathVariable(required = false) Integer id)
  {
    if (id == null)
      id = 1;
    Optional<Student> res = sr.findById(Long.valueOf(id));
    if (res.isPresent())
      return (res.get());
    else
      return new Student();
  }

  @RequestMapping("/hello")
  public Greeting sayHello(@RequestParam(value="name", defaultValue="World") String name)
  {
    return new Greeting("Spring Boot says 'Hello " + name + "'");
  }

}