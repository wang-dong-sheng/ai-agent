package com.dswang.aiagent.controller;/**
 * @author Mr.Wang
 * @create 2025-05-24-13:28
 */

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *@ClassName HealthContrller
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/24 13:28
 *@Version 1.0
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("")
    public String getHealth(){
        return "hello";
    }
}


