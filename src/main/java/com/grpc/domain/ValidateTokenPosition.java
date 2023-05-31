package com.grpc.domain;

import com.grpc.responses.ValidateTokenPositionResult;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import com.gpch.grpc.protobuf.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import responses.Error;

import java.io.*;
import java.util.*;

import static com.grpc.domain.Solver.*;

public class ValidateTokenPosition {
    Language language;
    private String taskInTTLFormat;
    private LinkedHashMap<String, String> studentAnswer; // key - item_0, value - word
    private String tokenToCheck; // word
    private Model model;
    ArrayList<String> wordsToSelect;

    public ValidateTokenPosition(
            Language language,
            String taskInTTLFormat,
            LinkedHashMap<String, String> studentAnswer,
            String tokenToCheck,
            ArrayList<String> wordsToSelect
    ) {
        System.out.println(language);
        System.out.println(studentAnswer);
        System.out.println(tokenToCheck);
        this.language = language;
        this.taskInTTLFormat = taskInTTLFormat;
        this.studentAnswer = studentAnswer;
        this.tokenToCheck = tokenToCheck;
        this.wordsToSelect = wordsToSelect;
        this.model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(taskInTTLFormat, "UTF-8"), null, "TTL");
    }

    public ValidateTokenPositionResult checkTokenPosition() throws IOException {
        Properties prop = new Properties();
        String fileName = "app.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        }
        String DIR_PATH_TO_TASK = prop.getProperty("app.path");

        ArrayList<Resource> hypotheses = new ArrayList<>();

        // Получить узел с токеном, который надо проверить
        String queryString = String.format("PREFIX ns1: <%s> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?resource " +
                "WHERE { " +
                "   ?resource rdfs:label \"%s\" . " +
                "}", NAMESPACE, tokenToCheck);

        // Execute the SPARQL query
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();

        // Process the query results
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Resource resource = solution.getResource("resource");
            hypotheses.add(resource);
//            println("Resource with label $tokenToCheck: ${resource.uri}");
        }

        // Close the query execution
        queryExecution.close();

        // для всех гипотез проверить на ошибки
        Property p = model.createProperty(NAMESPACE + "tokenPrecedes");
        Property x = model.createProperty(NAMESPACE + "var...");
        Resource prevToken = null;
        Resource currentToken = null;

        for (int i = 0; i < hypotheses.size(); i++) {
            Model hypothesisModel = model;
            for (Map.Entry<String, String> word : studentAnswer.entrySet()) {
                if (word.getKey().equals(tokenToCheck)) {
                    currentToken = hypotheses.get(i);
                    hypothesisModel.add(currentToken, x, "X");
                } else {
                    currentToken = hypothesisModel.getResource(NAMESPACE + word.getValue());
                }

                if (prevToken != null) {
                    hypothesisModel.add(prevToken, p, currentToken);
                }
                prevToken = currentToken;
            };

            // Write the model to a string in TTL format
            OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME + ".ttl");
            RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

            ArrayList<Error> res = new Solver(language.name(), DIR_PATH_TO_TASK).solve();
            if (res.isEmpty()) {
                studentAnswer.put(tokenToCheck, hypotheses.get(i).getLocalName());
                wordsToSelect.remove(tokenToCheck);

                return new ValidateTokenPositionResult(
                        res,
                        studentAnswer,
                        taskInTTLFormat,
                        wordsToSelect
                );
            }
            if (i == hypotheses.size() - 1) {
                studentAnswer.remove(tokenToCheck);
                wordsToSelect.add(tokenToCheck);

                return new ValidateTokenPositionResult(
                        res,
                        studentAnswer,
                        taskInTTLFormat,
                        wordsToSelect
                );
            }
        }

        return new ValidateTokenPositionResult(new ArrayList<>(), studentAnswer, taskInTTLFormat, wordsToSelect);
    }
}
