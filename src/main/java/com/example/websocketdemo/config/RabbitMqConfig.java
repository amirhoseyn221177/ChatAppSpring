package com.example.websocketdemo.config;


import com.rabbitmq.client.Connection;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;


@Configuration
public class RabbitMqConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter){
        RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter Jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory cachingConnectionFactory=new CachingConnectionFactory("localhost",5672);
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        return cachingConnectionFactory;
    }
    @Bean
    public AmqpAdmin amqpAdmin(){
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitManagementTemplate rabbitManagementTemplate(){
        return new RabbitManagementTemplate();
    }


//    @Bean
//    public Connection connection(){
//       com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
//
//        try {
//            factory.setUri("amqp://guest:guest@localhost:5672/virtualHost");
//            return factory.newConnection();
//        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | IOException | TimeoutException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}


//    @Bean
//    public AmqpAdmin amqpAdmin() {
//        return new RabbitAdmin(connectionFactory);
//    }
//    Then you add it to your service:
//
//@Autowired
//private AmqpAdmin admin;
//        Finally you can use it to create queues and bindings.
//
//        Queue queue = new Queue(queueName, durable, false, false);
//        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, EXCHANGE, routingKey, null);
//        admin.declareQueue(queue);
//        admin.declareBinding(binding);