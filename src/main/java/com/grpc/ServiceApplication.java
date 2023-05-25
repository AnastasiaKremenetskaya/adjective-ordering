package com.grpc;

import com.gpch.grpc.protobuf.Language;
import com.grpc.domain.ValidateTokenPosition;
import com.grpc.responses.ValidateTokenPositionResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
public class ServiceApplication {
//    static Path path = Paths.get(".").toAbsolutePath().normalize();
//    static String rules = path.toFile().getAbsolutePath() +"/src/main/resources/python/model.xml";

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ServiceApplication.class, args);


//        String content = new String(Files.readAllBytes(Path.of("/Users/anterekhova/IdeaProjects/adjective-ordering/src/main/resources/input_examples_adj/2.ttl")));
//
//        Map<String, String> tokens = new LinkedHashMap<>();
//        tokens.put("The", "item_0");
//        tokens.put("Japanese", "");
//        tokens.put("beautiful", "item_2");
//        tokens.put("books", "item_3");
//
//        ValidateTokenPositionResult res = new ValidateTokenPosition(
//                Language.EN,
//                content,
//                tokens,
//                "Japanese"
//        ).checkTokenPosition();
//        System.out.println(res.getErrors());
//        System.out.println(res.getTokenId());

//        GetTaskSetupResult res = new GetTaskSetup().getTask(content);
//        System.out.println(res);
    }
}
