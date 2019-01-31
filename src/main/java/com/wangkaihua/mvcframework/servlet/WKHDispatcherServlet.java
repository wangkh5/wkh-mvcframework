package com.wangkaihua.mvcframework.servlet;

import com.wangkaihua.mvcframework.annotation.*;
import com.wangkaihua.mvcframework.common.CommonUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * @desciption: 中控servlet
 * @author: wangkaihua
 * @date: 2019/1/21 21:06
 */
public class WKHDispatcherServlet extends HttpServlet {
    /**
     * web.xml文件中配置的servlet的配置文件的名称
     */
    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    /**
     * 保存加载到的配置文件
     */
    private Properties properties = new Properties();

    /**
     * 保存所有被扫描到的类的名称
     */
    private List<String> classNames = new ArrayList<String>();

    /**
     * 核心ioc容器保存所有的初始化bean
     */
    private Map<String, Object> ioc = new HashMap();

    /**
     * 保存所有的url和方法的映射关系
     */
    private Map<String, Method> handleMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req, resp);
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        // 没有任何url映射
        if (handleMapping.isEmpty()) {
            return;
        }

        // 获取请求的uri去掉项目名、去掉多余的反斜杠
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replace(contextPath, "").replaceAll("/+", "/");

        try {
            // 如果资源不存在
            if (!handleMapping.containsKey(uri)) {
                resp.getWriter().write("404 resource not found！");
                return;
            }

            // 资源存在
            Method method = handleMapping.get(uri);
            // 获取被调方法所有的参数
            Parameter[] parameters = method.getParameters();
            // 创建数组存储所有的参数值
            Object [] paramValues = new Object[parameters.length];
            for (int i = 0; i<parameters.length; i++) {
                // 如果参数类型是ServletRequest
                if (ServletRequest.class.isAssignableFrom(parameters[i].getType())) {
                    paramValues[i] = req;
                    continue;
                }
                // 如果参数类型是ServletResponse
                if (ServletResponse.class.isAssignableFrom(parameters[i].getType())) {
                    paramValues[i] = resp;
                    continue;
                }

                // 如果是其它参数类型先获取名和参数值
                // 获取参数名称判断参数是否定义了注解，获取注解中的参数名称，自定义参数必须写注解
                if (!parameters[i].isAnnotationPresent(WKHRequestParam.class)) {
                    continue;
                }
                String paramName = parameters[i].getAnnotation(WKHRequestParam.class).value().trim();
                String paramValue = req.getParameter(paramName);

                 // 获取请求的参数的键值对 name=lisi，age=23
                Map<String, String[]> parameterMap = req.getParameterMap();
                /*
               // 如果是String类型
                if (String.class.isAssignableFrom(parameters[i].getType())){
                    for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                        String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                                .replaceAll(",\\s", ",");
                        paramValues[i]=value;
                    }
                }*/

                // 如果是String类型
                if (String.class.isAssignableFrom(parameters[i].getType())){
                    paramValues[i] = paramValue;
                    continue;
                }
                // 如果是Integer类型
                if (Integer.class.isAssignableFrom(parameters[i].getType())) {
                    paramValues[i] = Integer.parseInt(paramValue);
                    continue;
                }
                // 如果是Float类型
                if (Float.class.isAssignableFrom(parameters[i].getType())) {
                    paramValues[i] = Float.parseFloat(paramValue);
                    continue;
                }
                // 如果是Double类型
                if (Double.class.isAssignableFrom(parameters[i].getType())) {
                    paramValues[i] = Double.parseDouble(paramValue);
                    continue;
                }
            }
            // 方法所属的类的类的首字母小写的名称
            String beanName = method.getDeclaringClass().getSimpleName();
            // 反射调用
            method.invoke(ioc.get(CommonUtils.toLowerFirstCase(beanName)),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("开始初始化dispacherservlet");
        // 1.加载配置文件
        doLoadConfig(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        // 2.扫描所有相关的类
        doScanner(properties.getProperty("scanPackage"));
        // 3.初始化所有相关类并保存到ioc容器中
        doInstance();
        // 4.依赖注入
        doAutowired();
        // 5.HandleMapping
        doInitHandleMapping();
        // 打印初始化完成提示信息
        System.out.println("wkhmvc is init ok");
    }

    private void doInitHandleMapping() {

        for (Map.Entry entry: ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            // 获取ioc容器中有WKHController的注解类
            if (!clazz.isAnnotationPresent(WKHController.class)) {
                 continue;
            }

            String baseUrl = "";
            // 判断类上是否有requestmapping注解
            if (clazz.isAnnotationPresent(WKHRequestMappring.class)) {
                WKHRequestMappring requestMappring = clazz.getAnnotation(WKHRequestMappring.class);
                baseUrl = requestMappring.value();
            }

            // 获取所有的方法判断方法上是否存在requestMapping注解
            Method[] methods = clazz.getMethods();
            for (Method method: methods) {
                if (!method.isAnnotationPresent(WKHRequestMappring.class)) {
                    continue;
                }
                String methodUrl = method.getAnnotation(WKHRequestMappring.class).value();
                // 保存所有的url和方法的映射关系
                String url = ("/"+baseUrl+"/"+methodUrl).replaceAll("/+", "/");
                handleMapping.put(url, method);
                System.out.println("mapping:"+url +"------------------"+method.getName());
            }

        }


    }

    private void doAutowired() {
        // 拿到bean中的所有带有注解的成员属性
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry entry : entries) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(WKHAutowired.class)) {
                    continue;
                }
                // 如果该字段存在注解需要注入
                WKHAutowired wkhAutowired= field.getAnnotation(WKHAutowired.class);
                String beanName = "";
                if ("".equals(wkhAutowired.value())) {
                    beanName = field.getName();
//                    beanName = field.getType().getName();
                } else {
                    beanName = wkhAutowired.value();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void doInstance() {
        try {
            for (String className: classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(WKHController.class)) {
                    // key: 类名首字母小写
                    String keyName = CommonUtils.toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(keyName, clazz.newInstance());
                    continue;
                }
                if (clazz.isAnnotationPresent(WKHService.class)) {
                    WKHService annotation = clazz.getAnnotation(WKHService.class);
                    // 如果用户没有设置自定义类名
                    if ("".equals(annotation.value().trim())) {
                        Class<?>[] interfaces = clazz.getInterfaces();
                        for (Class i: interfaces) {
                            ioc.put(CommonUtils.toLowerFirstCase(i.getSimpleName()), clazz.newInstance());
//                            ioc.put(i.getName(), i.newInstance());
                        }
                        continue;
                    }
                    // 如果用户设置了自定义类名
                    ioc.put(annotation.value(), clazz.newInstance());
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        String fileName = url.getFile();
        File dir = new File(fileName);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName+"."+file.getName());
            } else {
                classNames.add(packageName+"."+file.getName().replaceAll(".class", "").trim());
            }
        }
    }


    private void doLoadConfig(String configName) {
        try {
            InputStream fis = this.getClass().getClassLoader().getResourceAsStream(configName);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("加载配置文件失败");
        }
    }

}
