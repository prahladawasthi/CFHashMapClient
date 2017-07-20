package com;

import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cloudfoundry.HashServiceInfo;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping("/Hash/info")
    public HashServiceInfo info() {
        return hashServiceInfo();
    }

    @RequestMapping(value = "/Hash/{key}", method = RequestMethod.PUT)
    public ResponseEntity<String> put(@PathVariable("key") String key,
                                      @RequestBody String value) {
        restTemplate().put(hashServiceInfo().getUri()+"/{key}", value, key);
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    @RequestMapping(value = "/Hash/{key}", method = RequestMethod.GET)
    public ResponseEntity<String> put(@PathVariable("key") String key) {
        String response = restTemplate().getForObject(hashServiceInfo().getUri() + "/{key}", String.class, key);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Bean
    Cloud cloud() {
        return new CloudFactory().getCloud();
    }

    @Bean
    HashServiceInfo hashServiceInfo() {
        List<ServiceInfo> serviceInfos = cloud().getServiceInfos();
        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceInfo instanceof HashServiceInfo) {
                return (HashServiceInfo) serviceInfo;
            }
        }
        throw new RuntimeException("Unable to find bound Hash instance!");
    }

    @Bean
    RestTemplate restTemplate() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        BasicCredentialsProvider credentialsProvider =  new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(hashServiceInfo().getUsername(), hashServiceInfo().getPassword()));
        httpClient.setCredentialsProvider(credentialsProvider);
        ClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(rf);
    }
    
}
