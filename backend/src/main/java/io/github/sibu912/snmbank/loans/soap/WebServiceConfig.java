package io.github.sibu912.snmbank.loans.soap;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

/**
 * Spring Web Services set-up.
 * <ul>
 *   <li>SOAP requests go to /ws</li>
 *   <li>the WSDL is built from the XSD and served at /ws/loans.wsdl</li>
 *   <li>every request and response is checked against the XSD</li>
 * </ul>
 */
@EnableWs
@Configuration
public class WebServiceConfig implements WsConfigurer {

    public static final String NAMESPACE = "https://sibu912.github.io/snm-bank/loans/v1";

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        // Writes the real server address into the WSDL, e.g. http://localhost:8080/ws
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /** The bean name "loans" makes the WSDL available at /ws/loans.wsdl. */
    @Bean(name = "loans")
    public DefaultWsdl11Definition loansWsdl(XsdSchema loanApplicationSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("LoansPort");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace(NAMESPACE);
        wsdl.setSchema(loanApplicationSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema loanApplicationSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/loan-application.xsd"));
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        PayloadValidatingInterceptor validatingInterceptor = new PayloadValidatingInterceptor();
        validatingInterceptor.setXsdSchema(loanApplicationSchema());
        validatingInterceptor.setValidateRequest(true);
        validatingInterceptor.setValidateResponse(true);
        interceptors.add(validatingInterceptor);
    }
}
