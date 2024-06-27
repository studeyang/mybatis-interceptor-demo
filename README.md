# MyBatis插件实战运用

## 一、数据加密

### 1.1 背景

出于信息安全等法律条文规定，个人信息等敏感数据在存储时需进行加密。

### 1.2 技术实现

写入数据：通过 Mybatis 拦截器拦截 insert, update 语句，通过自定义注解获取到加密字段，并为该字段赋值密文，插入数据库中。

读取数据：通过 Mybatis 拦截器拦截 select 语句，通过自定义注解获取到加密字段，对密文进行解密，返回上层调用。

项目技术采用：SpringBoot2.1.7 + MyBatis + Maven3.5.4 + MySQL + Lombok(插件)

### 1.3 使用方式

在属性上添加`@EncryptField`注解后，就可以自动为该属性加解密。

```java
public class UserEntity {
    /**
     * 身份证
     */
    @EncryptField
    private String idCard;
    //其它属性 包括get, set方法
}
```

执行插入操作后，数据库里`id_card`就是密文了。

![image-20240705102053400](https://technotes.oss-cn-shenzhen.aliyuncs.com/2024/202407051021869.png)

执行查询操作，自动解密，返回上层调用。

```json
{
  "id": 682230480968224768,
  "name": "张三",
  "idCard": "442222111233322210",
  "sex": "男",
  "age": 0,
  "createTime": "2024-07-05 10:16:56",
  "updateTime": "2024-07-05 10:16:56",
  "status": 0
}
```

## 二、生成ID主键

### 2.1 背景

在生成表主键 ID 时，我们可以考虑主键自增或者 UUID，但它们都有很明显的缺点。

对于自增 ID 来说，第一，容易被爬虫遍历数据；第二，分表分库会有 ID 冲突。

对于 UUID 来说，数据太长，且有索引碎片、过多占用索引空间的问题。

雪花算法就很适合在分布式场景下生成唯一 ID，它既可以保证唯一又可以保证有序。

### 2.2 技术实现

通过 Mybatis 拦截器拦截 insert 语句，通过自定义注解获取到主键，并为该主键赋值雪花 ID，插入数据库中。

### 2.3 使用方式

在主键的属性上添加`@AutoId`注解后，就可以自动为该属性赋值主键 ID。

```java
public class UserEntity {
    /**
     * id(添加自定义注解)
     */
    @AutoId
    private Long id;
    /**
     * 姓名
     */
    private String name;
    //其它属性 包括get，set方法
}
```

执行插入操作后，数据库里就已经有雪花ID了。

![image-20240705102053400](https://technotes.oss-cn-shenzhen.aliyuncs.com/2024/202407051021869.png)

### 2.4 代码说明

在正式环境中只要涉及到`插入数据`的操作都被该插件拦截，并发量会很大。所以该插件代码即要保证`线程安全`又要保证`高可用`。所以在代码设计上做一些说明。

**1、线程安全**

产生雪花 ID 的时候必须是线程安全的，不能出现同一台服务器同一时刻出现了相同的雪花 ID，这里是通过：

```
单例模式 + synchronized
```

来实现的。

**2、高性能**

性能消耗比较大可能会出现在两个地方：

```
1）雪花算法生成雪花ID的过程。
2）通过类的反射机制找到哪些属性带有@AutoId注解的过程。
```

第一，生成雪花ID。简单测试过，生成20万条数据，大约在1.7秒，能满足我们实际开发中的需要。

第二，反射查找。可以在插件中添加缓存。

```java
/**
 * key值为Class对象 value可以理解成是该类带有AutoId注解的属性，只不过对属性封装了一层。
 * 它是非常能够提高性能的处理器 它的作用就是不用每次一个对象过来都要看下它的哪些属性带有AutoId注解
 * 毕竟类的反射在性能上并不友好。只要key包含该Class，那么下次同样的class进来，就不需要检查它哪些属性带AutoId注解。
 */
private Map<Class, List<Handler>> handlerMap = new ConcurrentHashMap<>();
```

插件部分源码

```java
public class AutoIdInterceptor implements Interceptor {
    /**
     * 处理器缓存
     */
    private Map<Class, List<Handler>> handlerMap = new ConcurrentHashMap<>();

    private void process(Object object) throws Throwable {
        Class handlerKey = object.getClass();
        List<Handler> handlerList = handlerMap.get(handlerKey);
        //先判断handlerMap是否已存在该class，不存在先找到该class有哪些属性带有@AutoId
        if (handlerList == null) {
            handlerMap.put(handlerKey, handlerList = new ArrayList<>());
            // 通过反射 获取带有AutoId注解的所有属性字段,并放入到handlerMap中
        }
         //为带有@AutoId赋值ID
        for (Handler handler : handlerList) {
            handler.accept(object);
        }
    }
}
```


