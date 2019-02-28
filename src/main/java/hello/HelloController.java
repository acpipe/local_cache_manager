package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class HelloController {

    @Autowired
    private AppCache appCache;

    private int count = 0;

    @RequestMapping(value = "/buildIndex", produces = {"application/json;charset=UTF-8"}, method = RequestMethod.POST)
    @ResponseBody
    public String buildIndex(@RequestBody String context) {
        System.out.println(context);
        return "{\"hello\":\"world\"}";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        System.out.println("context");
        return "unAddedKey" + ":" + appCache.getValueByKey("unAddedKey") + ":" + count;
    }

    @RequestMapping(value = "/test", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public String eventsMsg(@RequestParam String msg) {
        System.out.println(msg);
        return msg;
    }
}
