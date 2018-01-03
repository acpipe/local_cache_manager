package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloController {
    @RequestMapping("/")
    public String index() {
        log.info("start");
        return "Greetings from Spring Boot!";
    }
}
