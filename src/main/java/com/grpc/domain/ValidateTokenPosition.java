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
import responses.ErrorPart;

import java.io.*;
import java.util.*;

import static com.grpc.domain.Solver.*;

public final class ValidateTokenPosition {
    private static ValidateTokenPosition INSTANCE;

    private ValidateTokenPosition() {
    }

    public static ValidateTokenPosition getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ValidateTokenPosition();
        }

        return INSTANCE;
    }

    Language language;
    private String taskInTTLFormat;
    private LinkedHashMap<String, String> studentAnswer; // key - item_0, value - word
    private String tokenToCheck; // word
    private Model model;
    ArrayList<String> wordsToSelect;
    String DIR_PATH_TO_TASK;

    public ValidateTokenPositionResult checkTokenPosition(
            Language language,
            String taskInTTLFormat,
            LinkedHashMap<String, String> studentAnswer,
            String tokenToCheck,
            ArrayList<String> wordsToSelect
    ) throws IOException {
        this.language = language;
        this.taskInTTLFormat = taskInTTLFormat;
        this.studentAnswer = studentAnswer;
        this.tokenToCheck = tokenToCheck;
        this.wordsToSelect = wordsToSelect;
        this.model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(taskInTTLFormat, "UTF-8"), null, "TTL");

        Properties prop = new Properties();
        String fileName = "app.config";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        }
        this.DIR_PATH_TO_TASK = prop.getProperty("app.path");

        if (tokenToCheck.equals("-")) {
            return validateHyphen();
        }
        return validateWord();
    }

    private ValidateTokenPositionResult validateHyphen() throws FileNotFoundException {
        Model hypothesisModel = model;

        // для всех гипотез проверить на ошибки
        Property var = model.createProperty(NAMESPACE + "var...");

        Resource currentToken = null;

        boolean xAdded = false;
        for (Map.Entry<String, String> word : studentAnswer.entrySet()) {
            if (!word.getValue().equals("-")) {
                currentToken = hypothesisModel.getResource(NAMESPACE + word.getKey());

            }
            if (xAdded) {
                hypothesisModel.add(currentToken, var, "X");
                break;
            }
            if (word.getValue().equals("-")) {
                if (currentToken == null) {
                    return new ValidateTokenPositionResult(
                            new ArrayList<Error>(),
                            studentAnswer,
                            taskInTTLFormat,
                            wordsToSelect
                    );
                }

                hypothesisModel.add(currentToken, var, "Y");
                xAdded = true;
            }
        }
        ;

        // Write the model to a string in TTL format
        OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME + ".ttl");
        RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

        ArrayList<ErrorPart> res = Companion.getInstance().solveHyphen(language.name(), DIR_PATH_TO_TASK);

        ArrayList<Error> errors = new ArrayList<>();
        errors.add(new Error(res));

        LinkedHashMap<String, String> newStudentAnswer = new LinkedHashMap<>();
        if (res.isEmpty()) {
            for (Map.Entry<String, String> entry : studentAnswer.entrySet()) {
                if (entry.getKey().isEmpty()) {
                    newStudentAnswer.put(String.format("h_%d",System.currentTimeMillis()), tokenToCheck);
                } else {
                    newStudentAnswer.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            studentAnswer.remove(""); // удалить предположительный ответ
            newStudentAnswer = studentAnswer;
        }

        return new ValidateTokenPositionResult(
                errors,
                newStudentAnswer,
                taskInTTLFormat,
                wordsToSelect
        );
    }

    private ValidateTokenPositionResult validateWord() throws FileNotFoundException {
        ArrayList<Resource> hypotheses = new ArrayList<>();

        // Получить узел с токеном, который надо проверить
        String queryString = String.format("PREFIX ns1: <%s> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?resource " +
                "WHERE { " +
                "   ?resource rdfs:label \"%s\" . " +
                "} order by asc(?resource)", NAMESPACE, tokenToCheck);

        // Execute the SPARQL query
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();

        // Process the query results
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Resource resource = solution.getResource("resource");
            hypotheses.add(resource);
        }
        queryExecution.close();

        Resource newWordResource = hypotheses.get(0);
        LinkedHashMap<String, String> newStudentAnswer = new LinkedHashMap<>();
        String hypothesysId = "";

        int i = 0;
        while (i < hypotheses.size()) {
            for (Map.Entry<String, String> entry : studentAnswer.entrySet()) {
                if (entry.getValue().equals(tokenToCheck)) {
                    String id = hypotheses.get(i).getLocalName().toString();
                    newStudentAnswer.put(id, tokenToCheck);
                    if (entry.getKey().isEmpty()) {
                        newWordResource = hypotheses.get(i);
                        hypothesysId = id;
                    }
                    i++;
                } else {
                    newStudentAnswer.put(entry.getKey(), entry.getValue());
                }
            }
        }

        Model hypothesisModel = buildModelFromStudentAnswer(newStudentAnswer, newWordResource);

        // Write the model to a string in TTL format
        OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME + ".ttl");
        RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

        ArrayList<ErrorPart> res = Companion.getInstance().solve(language.name(), DIR_PATH_TO_TASK);

        ArrayList<Error> errors = new ArrayList<>();
        errors.add(new Error(res));

        if (res.isEmpty()) {
            wordsToSelect.remove(tokenToCheck);

            return new ValidateTokenPositionResult(
                    errors,
                    newStudentAnswer,
                    taskInTTLFormat,
                    wordsToSelect
            );
        }

        // Если проверили все гипотезы, но каждая ошибочна - возвращаем ошибку
        newStudentAnswer.remove(hypothesysId); // удалить предположительный ответ

        return new ValidateTokenPositionResult(
                errors,
                newStudentAnswer,
                taskInTTLFormat,
                wordsToSelect
        );
    }

    private Model buildModelFromStudentAnswer(LinkedHashMap<String, String> studentAnswer, Resource hypothesis) {
        Model hypothesisModel = model;

        // для всех гипотез проверить на ошибки
        Property p = model.createProperty(NAMESPACE + "tokenPrecedes");
        Property x = model.createProperty(NAMESPACE + "var...");
        Resource prevToken = null;
        Resource currentToken = null;

        for (Map.Entry<String, String> word : studentAnswer.entrySet()) {
            String a = hypothesis.getLocalName();
            if (word.getKey().equals(a)) {
                currentToken = hypothesis;
                hypothesisModel.add(currentToken, x, "X");
            } else {
                currentToken = hypothesisModel.getResource(NAMESPACE + word.getKey());
            }

            if (prevToken != null) {
                hypothesisModel.add(prevToken, p, currentToken);
            }
            prevToken = currentToken;
        }
        ;

        return hypothesisModel;
    }
}
