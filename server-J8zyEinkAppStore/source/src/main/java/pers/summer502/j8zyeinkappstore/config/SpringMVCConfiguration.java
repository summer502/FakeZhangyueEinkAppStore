package pers.summer502.j8zyeinkappstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pers.summer502.j8zyeinkappstore.util.HTTPAgent;

@Configuration
public class SpringMVCConfiguration implements WebMvcConfigurer {
    @Autowired
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HTTPAgent())
                .addPathPatterns("/**")
                .excludePathPatterns("/EinkAppStore", "/EinkAppStore/**");

    }

//    @Override
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//        configurer.setTaskExecutor(asyncTaskExecutor);
//        configurer.setDefaultTimeout(60 * 1000L);
//    }

}
