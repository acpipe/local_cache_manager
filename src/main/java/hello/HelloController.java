package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloController {
    @Autowired
    private AppCache appCache;

    @RequestMapping("/")
    public String index() {
        return "unAddedKey" + ":" + appCache.getValueByKey("unAddedKey");
    }
}
