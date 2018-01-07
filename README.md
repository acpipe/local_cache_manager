# local_cache_manager

本地缓存管理的demo.

## quick start

```
git clone git@github.com:Acceml/local_cache_manager.git
mvn package && java -jar target/local_cache_manager-0.1.0.jar
```

在浏览器输入：http://localhost:9294/

返回：unAddedKey:not find in appCache

测试成功。

# 如何添加自己的缓存

可参考代码里面的[demo](https://github.com/Acceml/local_cache_manager/blob/master/src/main/java/hello/AppCache.java)

## 1. 继承BaseCacheUpdateJob

## 2. 重写getPeriodInSecond() 设置缓存更新时间

## 3.重写isEssential()方法设置是不是首次启动必须加载的缓存