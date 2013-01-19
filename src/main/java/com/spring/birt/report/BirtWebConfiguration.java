package com.spring.birt.report;

import com.spring.birt.core.BirtEngineFactory;
import com.spring.birt.core.BirtView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;

@EnableWebMvc 
@ComponentScan( {"com.spring.birt.core", "com.spring.birt.report"})
@Configuration
public class BirtWebConfiguration  extends WebMvcConfigurerAdapter  {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/reports").setViewName("birtView");

	}

	@Bean 
	public BirtView birtView(){
        logger.debug("-= birtView =-");
		BirtView bv = new BirtView();
		bv.setBirtEngine( this.engine().getObject() );
		return bv; 
	}


	@Bean public BeanNameViewResolver beanNameResolver(){
        logger.debug("-= beanNameResolver =-");
		BeanNameViewResolver br = new BeanNameViewResolver() ;
		return br; 
	} 

	@Bean
	protected BirtEngineFactory engine(){
        logger.debug("-= BirtEngineFactory =-");
        BirtEngineFactory factory = new BirtEngineFactory() ;
		return factory ; 
	}
	

} 