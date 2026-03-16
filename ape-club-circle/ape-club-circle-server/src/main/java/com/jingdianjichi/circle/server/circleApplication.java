package com.jingdianjichi.circle.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 练习微服务启动类
 *
 * @author: ChickenWing
 * @date: 2024/3/2
 */
@SpringBootApplication
@ComponentScan("com.jingdianjichi")
@MapperScan("com.jingdianjichi.**.dao")
@EnableFeignClients(basePackages = "com.jingdianjichi")
public class circleApplication {

    public static void main(String[] args) {
        SpringApplication.run(circleApplication.class);
        System.out.println("\n" +
                " █████╗ ██████╗ ███████╗ ██████╗██╗     ██╗   ██╗██████╗  ██████╗██╗██████╗  ██████╗██╗     ███████╗\n"+
                "██╔══██╗██╔══██╗██╔════╝██╔════╝██║     ██║   ██║██╔══██╗██╔════╝██║██╔══██╗██╔════╝██║     ██╔════╝\n"+
                "███████║██████╔╝█████╗  ██║     ██║     ██║   ██║██████╔╝██║     ██║██████╔╝██║     ██║     █████╗  \n"+
                "██╔══██║██╔═══╝ ██╔══╝  ██║     ██║     ██║   ██║██╔══██╗██║     ██║██╔══██╗██║     ██║     ██╔══╝  \n"+
                "██║  ██║██║     ███████╗╚██████╗███████╗╚██████╔╝██████╔╝╚██████╗██║██║  ██║╚██████╗███████╗███████╗\n"+
                "╚═╝  ╚═╝╚═╝     ╚══════╝ ╚═════╝╚══════╝ ╚═════╝ ╚═════╝  ╚═════╝╚═╝╚═╝  ╚═╝ ╚═════╝╚══════╝╚══════╝\n"+
                "\n" +
                "ApeClubCircle Service started successfully\n" +
                "Circle and community service is ready\n"
        );
    }

}
