package com.jingdianjichi.club.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关启动器
 * 
 * @author: ChickenWing
 * @date: 2023/10/11
 */

//鉴权模块
@SpringBootApplication
@ComponentScan("com.jingdianjichi")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class);
        System.out.println("\n" +
                " █████╗ ██████╗ ███████╗ ██████╗██╗     ██╗   ██╗██████╗  ██████╗  █████╗ ████████╗███████╗██╗    ██╗ █████╗ ██╗   ██╗\n"+
                "██╔══██╗██╔══██╗██╔════╝██╔════╝██║     ██║   ██║██╔══██╗██╔════╝ ██╔══██╗╚══██╔══╝██╔════╝██║    ██║██╔══██╗╚██╗ ██╔╝\n"+
                "███████║██████╔╝█████╗  ██║     ██║     ██║   ██║██████╔╝██║  ███╗███████║   ██║   █████╗  ██║ █╗ ██║███████║ ╚████╔╝ \n"+
                "██╔══██║██╔═══╝ ██╔══╝  ██║     ██║     ██║   ██║██╔══██╗██║   ██║██╔══██║   ██║   ██╔══╝  ██║███╗██║██╔══██║  ╚██╔╝  \n"+
                "██║  ██║██║     ███████╗╚██████╗███████╗╚██████╔╝██████╔╝╚██████╔╝██║  ██║   ██║   ███████╗╚███╔███╔╝██║  ██║   ██║   \n"+
                "╚═╝  ╚═╝╚═╝     ╚══════╝ ╚═════╝╚══════╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝ ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝   \n"+
                "\n" +
                "ApeClubGateway Service started successfully\n" +
                "Gateway routing and authentication service is ready\n"
        );
    }

}
