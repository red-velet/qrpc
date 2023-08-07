package icu.chiou;


import icu.chiou.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: 服务调用方启动器
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) throws InterruptedException {
        //todo 服务消费者需要做的事情：获取具体待消费对象实例(代理对象:封装连接、获取对象)
        //reference进行代理,其中封装连接、返回对象
        ReferenceConfig<HelloQRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloQRpc.class);

        //代理具体要干的事情
        //1.连接注册中心
        //2.拉取服务列表
        //3.选择一个服务,与其建立连接
        //4.发送请求,携带参数(接口名、方法名、参数列表),获得响应
        QRpcBootstrap.getInstance()
                .application("first-qrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        HelloQRpc helloQRpc = reference.get();

        while (true) {
            try {
                Thread.sleep(2000);
                log.info("++++++++>>>>>>>>>>>>>>>>>>>>>>===============>>>>>>>>>>>>>>>>>>>");
                //获取代理对象
                String msg = "计算序列化前后对象的大小可以分别获取对象在内存中的大小和序列化后的大小，并进行对比。以下是计算序列化前后对象大小的方法：\n" +
                        "\n" +
                        "1. 计算对象在内存中的大小：\n" +
                        "\n" +
                        "可以使用Java的`ObjectSizeCalculator`类，这个类可以计算对象在内存中的大小。但是需要注意，这种方法计算的大小并不是完全准确的，因为Java对象在内存中可能包含一些额外的开销，比如对象头、对齐等。\n" +
                        "\n" +
                        "```java\n" +
                        "import java.lang.instrument.Instrumentation;\n" +
                        "\n" +
                        "public class ObjectSizeCalculator {\n" +
                        "    private static Instrumentation instrumentation;\n" +
                        "\n" +
                        "    public static void premain(String args, Instrumentation inst) {\n" +
                        "        instrumentation = inst;\n" +
                        "    }\n" +
                        "\n" +
                        "    public static long getObjectSize(Object obj) {\n" +
                        "        if (instrumentation == null) {\n" +
                        "            throw new IllegalStateException(\"Instrumentation not initialized\");\n" +
                        "        }\n" +
                        "        return instrumentation.getObjectSize(obj);\n" +
                        "    }\n" +
                        "\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Object obj = new SomeObject();\n" +
                        "        long sizeInMemory = getObjectSize(obj);\n" +
                        "        System.out.println(\"Object size in memory: \" + sizeInMemory + \" bytes\");\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n" +
                        "\n" +
                        "2. 计算序列化后对象的大小：\n" +
                        "\n" +
                        "计算序列化后对象的大小可以通过将对象序列化成字节数组，然后计算字节数组的长度来得到。\n" +
                        "\n" +
                        "```java\n" +
                        "import java.io.ByteArrayOutputStream;\n" +
                        "import java.io.IOException;\n" +
                        "import java.io.ObjectOutputStream;\n" +
                        "\n" +
                        "public class SerializationSizeCalculator {\n" +
                        "    public static int getSerializedObjectSize(Object obj) throws IOException {\n" +
                        "        ByteArrayOutputStream bos = new ByteArrayOutputStream();\n" +
                        "        ObjectOutputStream oos = new ObjectOutputStream(bos);\n" +
                        "        oos.writeObject(obj);\n" +
                        "        oos.flush();\n" +
                        "        byte[] byteArray = bos.toByteArray();\n" +
                        "        oos.close();\n" +
                        "        bos.close();\n" +
                        "        return byteArray.length;\n" +
                        "    }\n" +
                        "\n" +
                        "    public static void main(String[] args) throws IOException {\n" +
                        "        Object obj = new SomeObject();\n" +
                        "        int sizeAfterSerialization = getSerializedObjectSize(obj);\n" +
                        "        System.out.println(\"Serialized object size: \" + sizeAfterSerialization + \" bytes\");\n" +
                        "    }\n" +
                        "}\n" +
                        "```\n" +
                        "\n" +
                        "请注意，这两种方法计算的大小都有其局限性，对于复杂的对象，可能不会完全准确反映对象的实际大小。在实际使用中，需要根据具体情况选择合适的方法来计算对象大小。";
                for (int i = 0; i < 5; i++) {
                    String love = helloQRpc.say(msg);
                    log.info("远程方法调用返回 --> ❤️ {}", love);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
//        for (int i = 0; i < 500; i++) {
//            try {
//                log.info("++++++++>>>>>>>>>>>>>>>>>>>>>>===============>>>>>>>>>>>>>>>>>>>");
//                //获取代理对象
//                String love = helloQRpc.say("i love you");
//                log.info("远程方法调用返回 --> ❤️ {}", love);
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
