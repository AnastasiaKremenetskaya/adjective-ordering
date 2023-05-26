package com.grpc;

import its.model.DomainModel;
import its.model.dictionaries.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import static com.grpc.domain.Solver.DIR_PATH_TO_TASK;

@SpringBootApplication
public class ServiceApplication {
//    static Path path = Paths.get(".").toAbsolutePath().normalize();
//    static String rules = path.toFile().getAbsolutePath() +"/src/main/resources/python/model.xml";

    public static void main(String[] args) throws IOException {
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
