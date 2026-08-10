package com.example.hrms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 中小企业员工考勤与薪资核算管理系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.example.hrms.mapper")
public class HrmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);
        System.out.println("\n================================================");
        System.out.println("  HRMS 考勤薪资系统启动成功！");
        System.out.println("  前端/系统访问:  http://localhost:8080");
        System.out.println("  H2 控制台:      http://localhost:8080/h2-console");
        System.out.println("  默认管理员:     admin / admin123");
        System.out.println("  默认员工:       zhangsan / 123456");
        System.out.println("================================================\n");
    }
}
