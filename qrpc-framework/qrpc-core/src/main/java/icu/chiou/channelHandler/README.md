### 1.服务调用方

通过netty发送报文，`writeAndFlush(object)`写出去
pipeline开始生效

- 第一个处理器(out) --> 转化object -> msg请求
- 第二个处理器(out) --> 序列化
- 第三个处理器(out) --> 压缩

### 2.服务提供方

通过netty接收报文
pipeline开始生效

- 第一个处理器(in) --> 解压缩
- 第二个处理器(in) --> 反序列化
- 第三个处理器(in) --> 解析报文

### 3.执行方法调用，返回结果

### 4.服务提供方

通过netty发送报文，`writeAndFlush(object)`写出去
pipeline开始生效

- 第一个处理器(out) --> 转化object -> msg请求
- 第二个处理器(out) --> 序列化
- 第三个处理器(out) --> 压缩

### 5.服务调用方

通过netty接收报文
pipeline开始生效

- 第一个处理器(in) --> 解压缩
- 第二个处理器(in) --> 反序列化
- 第三个处理器(in) --> 解析报文

### 6.得到结果返回