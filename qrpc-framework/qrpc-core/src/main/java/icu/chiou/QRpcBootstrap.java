package icu.chiou;

import icu.chiou.annotation.QRpcApi;
import icu.chiou.channelhandler.handler.MethodInvokeHandler;
import icu.chiou.channelhandler.handler.decoder.QRpcRequestDecoder;
import icu.chiou.channelhandler.handler.encoder.QRpcResponseEncoder;
import icu.chiou.config.Configuration;
import icu.chiou.core.HeartbeatDetector;
import icu.chiou.core.QRpcShutdownHook;
import icu.chiou.discovery.Registry;
import icu.chiou.discovery.RegistryConfig;
import icu.chiou.loadbalancer.LoadBalancer;
import icu.chiou.transport.message.QRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Author: chiou
 * createTime: 2023/7/23
 * Description: No Description
 */
@Slf4j
@Getter
public class QRpcBootstrap {
    //QRpcBootstrap是个单例,每个应用只有一个
    private final static QRpcBootstrap BOOTSTRAP = new QRpcBootstrap();

    //全局配置中心
    private Configuration configuration;

    //维护已经且发布的服务列表
    public final static Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //netty的连接缓存-channel
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    //响应时间
    public final static TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //全局被挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    public static final ThreadLocal<QRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();


    private QRpcBootstrap() {
        //构造启动程序时需要做一些初始化
        configuration = new Configuration();
    }

    public static QRpcBootstrap getInstance() {
        return BOOTSTRAP;
    }

    /**
     * 该方法用于设置应用名
     *
     * @param applicationName 应用名
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap application(String applicationName) {
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 该方法用于设置注册中心
     *
     * @param registryConfig 注册中心
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 该方法用于设置负载均衡策略
     *
     * @param loadBalancer 负载均衡策略
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    //---------------------------------服务提供方的api-------------------------------------------------

    /**
     * 该方法用于发布服务
     * 发布服务的核心:将接口的实现注册到服务中心
     *
     * @param service 独立封装的需要发布的服务
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap publish(ServiceConfig<?> service) {
        //抽象了注册中心的概念
        //把服务发布到注册中心
        configuration.getRegistryConfig().getRegistry().register(service);
        //1.当服务调用方，通过接口、方法名、参数列表调用方法时，怎么知道是具体哪个实例的方法呢？
        //(1) new一个 (2) spring beadFactory.getBean(class) (3) 手动维护映射院系
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 该方法用于批量发布服务
     * 发布服务的核心:将接口的实现注册到服务中心
     *
     * @param services 封装的需要发布的服务集合
     * @return this-返回当前实例对象
     */
    public QRpcBootstrap publish(List<?> services) {
        return this;
    }

    /**
     * 该方法用于启动服务(netty服务)
     */
    public void start() {
        //注册关闭应用程序的钩子函数
        Runtime.getRuntime().addShutdownHook(new QRpcShutdownHook());
        //启动netty服务
        EventLoopGroup boss = null;
        EventLoopGroup worker = null;
        try {
            //1.创建EventGroup
            boss = new NioEventLoopGroup(2);
            worker = new NioEventLoopGroup(10);

            //2.服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new QRpcRequestDecoder())
                                    .addLast(new MethodInvokeHandler())
                                    .addLast(new QRpcResponseEncoder());
                        }
//                        @Override
//                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(new SimpleChannelInboundHandler() {
//
//                                @Override
//                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                    ByteBuf buf = (ByteBuf) msg;
//                                    log.info("服务提供者收到消息:---> {}", buf.toString(StandardCharsets.UTF_8));
//                                    //回应
//                                    ctx.channel()
//                                            .writeAndFlush(Unpooled.copiedBuffer("i like you to".getBytes()));
//                                }
//                            });
//                        }
                    });

            //3.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();

            //4.获取数据
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //---------------------------------服务提供方的api-------------------------------------------------


    //---------------------------------服务调用方的api-------------------------------------------------

    public QRpcBootstrap reference(ReferenceConfig<?> reference) {
        //在这个方法里我们是否可以拿到相关的配置项: 如注册中心,分组信息-在调用时才能有调用的能力,并且确认调用哪个分组的
        //配置reference,便于后面调用get方法时,生成代理对象
        //开启对整个服务的心跳检测
        HeartbeatDetector.detectorHeaderDance(reference.getInterface().getName());

        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(configuration.getGroup());
        return this;
    }

    public QRpcBootstrap serialize(String serializeType) {
        if (serializeType != null) {
            configuration.setSerializeType(serializeType);
        }
        if (log.isDebugEnabled()) {
            log.debug("配置了使用序列化的方式为【{}】", configuration.getSerializeType());
        }
        return this;
    }

    public QRpcBootstrap compress(String compressType) {
        if (compressType != null) {
            configuration.setCompressType(compressType);
        }
        if (log.isDebugEnabled()) {
            log.debug("配置了使用压缩的算法为【{}】", configuration.getCompressType());
        }
        return this;
    }

    public QRpcBootstrap group(String group) {
        if (group != null) {
            configuration.setGroup(group);
        }
        if (log.isDebugEnabled()) {
            log.debug("配置了分组属于【{}】", configuration.getGroup());
        }
        return this;
    }

    public Registry getRegistry() {
        return configuration.getRegistryConfig().getRegistry();
    }

    public QRpcBootstrap scan(String packageName) {
        //1.获取packageName下的所有类的全限定名称
        List<String> classNameList = getAllClassName(packageName);

        //2.通过反射获取它的接口,创建具体实现
        List<Class<?>> classList = classNameList.stream().map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter(clazz -> clazz.getAnnotation(QRpcApi.class) != null
        ).collect(Collectors.toList());

        //3.发布服务
        Object instance = null;
        for (Class<?> clazz : classList) {
            Class<?>[] interfaces = clazz.getInterfaces();
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            //获取分组信息
            String group = clazz.getAnnotation(QRpcApi.class).group();

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                publish(serviceConfig);
                if (log.isDebugEnabled()) {
                    log.debug("✔️✔️✔️✔️已经通过包扫描将【{}】服务发布", anInterface.getName());
                }
            }
        }
        return this;
    }

    private List<String> getAllClassName(String packageName) {
        //1.通过packageName获取绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时,路径不在");
        }
        String absolutePath = url.getPath();
        //递归获取所有文件路径
        List<String> classNames = new ArrayList<>();

        classNames = recursionFile(absolutePath, classNames, basePath);
        System.out.println(classNames);
        //2.
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            //获取该目录下的所有class文件和子目录
            File[] childFile = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (childFile == null || childFile.length == 0) {
                return null;
            }
            //遍历再继续判断子目录和文件
            for (File child : childFile) {
                if (child.isDirectory()) {
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    System.out.println(className);
                    classNames.add(className);
                }
            }


        } else {
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            System.out.println(className);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String s = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\"))).replaceAll("\\\\", ".");
        return s.substring(0, s.indexOf(".class"));
    }


    //---------------------------------服务调用方的api-------------------------------------------------

}
