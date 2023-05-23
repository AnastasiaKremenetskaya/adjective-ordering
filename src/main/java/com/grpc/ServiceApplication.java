package com.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.io.*;
import java.util.*;

@SpringBootApplication
public class ServiceApplication {
//    static Path path = Paths.get(".").toAbsolutePath().normalize();
//    static String rules = path.toFile().getAbsolutePath() +"/src/main/resources/python/model.xml";

    public static void main(String[] args) throws IOException {
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("item_0","beautiful");
        tokens.put("","The");
        tokens.put("item_2","books");

        OrderCheckerResult res = new StudentResponseFormatter(
                1,
                tokens,
                "The"
        ).checkTokenPosition();
        System.out.println(res.getAdjectiveOrderingErrors());
//        String content = new String(Files.readAllBytes(Paths.get(rules)));
//        SpringApplication.run(ServiceApplication.class, args);
//        AdjectiveOrderChecker.getAdjectiveOrderingErrors(content);
    }
}
