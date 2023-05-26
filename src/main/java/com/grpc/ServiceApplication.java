package com.grpc;

import its.model.DomainModel;
import its.model.dictionaries.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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


//        String content = new String(Files.readAllBytes(Path.of("/Users/anterekhova/IdeaProjects/adjective-ordering/src/main/resources/input_examples_adj/2.ttl")));
//
//        Map<String, String> tokens = new LinkedHashMap<>();
//        tokens.put("The", "item_0");
//        tokens.put("Japanese", "");
//        tokens.put("beautiful", "item_2");
//        tokens.put("books", "item_3");
//
//        com.grpc.responses.ValidateTokenPositionResult res = new ValidateTokenPosition(
//                Language.EN,
//                content,
//                (LinkedHashMap<String, String>) tokens,
//                "Japanese"
//        ).checkTokenPosition();
//        System.out.println(res.getErrors());

//        GetTaskSetupResult res = new GetTaskSetup().getTask(content);
//        System.out.println(res);
    }
}
