package com.wayble.server;
import org.springframework.stereotype.Service;

@Service
public class CodeRabbitTest {

    public void test1() {
        int a = 5;
        int b = 0;
        int result = a / b;
        System.out.println("결과: " + result);
    }

    public void test2() {
        for (int i = 0; i < 100; i++) {
            System.out.println("반복문 실행: " + i);
        }
    }
}
