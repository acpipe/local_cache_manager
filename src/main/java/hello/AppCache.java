package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AppCache extends BaseCacheUpdateJob {

    private Map<String, String> map = new HashMap<>();

    @Autowired
    public AppCache() {
    }

    @Override
    public long getPeriodInSecond() {
        return PERIOD_ONE_MINUTE;
    }

    @Override
    public boolean isEssential() {
        return true;
    }

    public String getValueByKey(String appId) {
        return map.getOrDefault(appId, "not find in appCache");
    }

    @Override
    public boolean update() {
        map.put("add", "0");
        return true;
    }
}
