//package t3h.edu.vn.traintickets.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.thymeleaf.spring6.SpringTemplateEngine;
//import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
//
//@Configuration
//public class ThymeleafConfig {
//
//    @Bean
//    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver resolver) {
//        SpringTemplateEngine engine = new SpringTemplateEngine();
//        engine.setTemplateResolver(resolver);
//
//        // Thêm #numbers
//        engine.addDialect(new Java8TimeDialect()); // nếu cần format thời gian
//        engine.setEnableSpringELCompiler(true);
//
//        return engine;
//    }
//}
