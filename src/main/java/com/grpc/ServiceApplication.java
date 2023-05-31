package com.grpc;

import com.gpch.grpc.protobuf.Language;
import com.grpc.domain.ValidateTokenPosition;
import its.model.DomainModel;
import its.model.dictionaries.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class ServiceApplication {
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        String fileName = "app.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        }
        String DIR_PATH_TO_TASK = prop.getProperty("app.path");

        System.out.println(DIR_PATH_TO_TASK);
        System.out.println(Paths.get(".").toAbsolutePath().normalize().toString());
        new DomainModel(
                new ClassesDictionary(),
                new DecisionTreeVarsDictionary(),
                new EnumsDictionary(),
                new PropertiesDictionary(),
                new RelationshipsDictionary(),
                DIR_PATH_TO_TASK
        );

        SpringApplication.run(ServiceApplication.class, args);


//        String content = new String(Files.readAllBytes(Path.of("/Users/anterekhova/IdeaProjects/adjective-ordering/src/main/resources/input_examples_adj/7.ttl")));
//
//        ArrayList<String> wordsToSelect = new ArrayList<>();
//        wordsToSelect.add("Japanese");
//        wordsToSelect.add("amazing");

//        Map<String, String> tokens = new LinkedHashMap<>();
////        tokens.put("amazing", "item_0");
//        tokens.put("young", "item_1");
//        tokens.put("amazing", "");
////        tokens.put("-", "");
//        tokens.put("cod", "item_4");
//        tokens.put("sellers", "item_5");

//        tokens.put("salt", "item_1");
//        tokens.put("-", "item_2");
//        tokens.put("cod", "item_3");
//        tokens.put("Japanese", "");
//        tokens.put("sellers", "item_4");

// ttl2
//        tokens.put("The", "item_0");
//        tokens.put("Japanese", "");
//        tokens.put("beautiful", "item_2");
//        tokens.put("books", "item_3");

// ttl0
//        tokens.put("Amazing", "item_0");
//        tokens.put("young", "item_1");
//        tokens.put("big", "item_2");
//        tokens.put("-", "item_3");
//        tokens.put("amazing", "item_4");
//        tokens.put("-", "item_5");
//        tokens.put("taste", "item_6");
//        tokens.put("-", "");
//        tokens.put("cod", "item_8");
//        tokens.put("sellers", "item_9");

//        com.grpc.responses.ValidateTokenPositionResult res = new ValidateTokenPosition(
//                Language.EN,
//                content,
//                (LinkedHashMap<String, String>) tokens,
//                "Japanese",
//                wordsToSelect
//
//        ).checkTokenPosition();
//        System.out.println(res.getErrors());
//        System.out.println(wordsToSelect);
//        System.out.println(tokens);

//        GetTaskSetupResult res = new GetTaskSetup().getTask(content);
//        System.out.println(res);
    }
}
