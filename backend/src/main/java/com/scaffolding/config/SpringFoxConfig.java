package com.scaffolding.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

import java.lang.reflect.Method;

/**
 * SpringFox兼容性配置
 * 解决Spring Boot 2.7与SpringFox 3.0.0的兼容性问题
 * 
 * @author scaffolding
 */
@Configuration
public class SpringFoxConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    /**
     * 修复 Springfox 3.0.0 与 Spring Boot 2.7+ 的兼容性问题
     * 使用 BeanPostProcessor 包装 DocumentationPluginsBootstrapper，捕获启动异常
     */
    @Bean
    public static BeanPostProcessor documentationPluginsBootstrapperPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DocumentationPluginsBootstrapper && "documentationPluginsBootstrapper".equals(beanName)) {
                    DocumentationPluginsBootstrapper original = (DocumentationPluginsBootstrapper) bean;
                    // 创建一个实现了 Lifecycle 接口的包装对象
                    return new SafeDocumentationPluginsBootstrapper(original);
                }
                return bean;
            }
        };
    }

    /**
     * 安全的 DocumentationPluginsBootstrapper 包装类
     * 实现 Lifecycle 接口，在 start 方法中捕获异常，避免应用启动失败
     */
    private static class SafeDocumentationPluginsBootstrapper implements Lifecycle {
        private final DocumentationPluginsBootstrapper delegate;
        private boolean running = false;

        public SafeDocumentationPluginsBootstrapper(DocumentationPluginsBootstrapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public void start() {
            try {
                // 使用反射调用 start 方法
                Method startMethod = ReflectionUtils.findMethod(delegate.getClass(), "start");
                if (startMethod != null) {
                    ReflectionUtils.invokeMethod(startMethod, delegate);
                    running = true;
                } else {
                    // 如果找不到方法，直接标记为运行中
                    System.err.println("警告: 无法找到 DocumentationPluginsBootstrapper.start() 方法，跳过 Swagger 初始化");
                    running = true;
                }
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                // 捕获并忽略 SpringFox 兼容性异常，允许应用继续启动
                if (cause instanceof NullPointerException) {
                    String message = cause.getMessage() != null ? cause.getMessage() : "";
                    if (message.contains("PatternsRequestCondition") || message.contains("condition")) {
                        System.err.println("警告: Springfox 初始化时遇到兼容性问题（Spring Boot 2.7+），但应用将继续启动");
                        System.err.println("提示: Swagger UI 可能无法正常访问，但不影响其他 API 功能");
                        running = true; // 标记为已启动，允许应用继续运行
                    } else {
                        // 其他类型的 NullPointerException，也允许继续启动
                        System.err.println("警告: Springfox 初始化异常: " + cause.getMessage());
                        running = true;
                    }
                } else {
                    // 非 NullPointerException 的异常，记录但允许继续启动
                    System.err.println("警告: Springfox 启动时发生异常，但应用将继续启动: " + e.getMessage());
                    if (cause != null && cause != e) {
                        System.err.println("原因: " + cause.getMessage());
                    }
                    running = true;
                }
            }
        }

        @Override
        public void stop() {
            try {
                Method stopMethod = ReflectionUtils.findMethod(delegate.getClass(), "stop");
                if (stopMethod != null) {
                    ReflectionUtils.invokeMethod(stopMethod, delegate);
                }
                running = false;
            } catch (Exception e) {
                // 忽略停止时的异常
                running = false;
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        // 代理其他方法调用到原始对象
        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
