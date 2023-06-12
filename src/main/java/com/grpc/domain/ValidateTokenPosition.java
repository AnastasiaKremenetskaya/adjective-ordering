package com.grpc.domain;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableMap;
import com.grpc.responses.ValidateTokenPositionResult;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
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

    static final ImmutableMap<Language, ArrayList<String>> FINISH_ERR = ImmutableMap.<Language, ArrayList<String>>builder()
            .put(Language.RU, new ArrayList<>(
                            Arrays.asList(
                                    "Справа от",
                                    "отсутствует дефис, в то время как это слово является частью сложного прилагательного и должно быть соединено дефисом со своим главным словом"
                            )
                    )
            )
            .put(Language.EN, new ArrayList<>(
                            Arrays.asList(
                                    "No hyphen right to",
                                    "whether this word is a part of compond adjective and should be hyphened with main word"
                            )
                    )
            )
            .build();

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

        if (wordsToSelect.contains("-") && wordsToSelect.size() == 1 && tokenToCheck.isEmpty()) {
            return validateFinish();
        }
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
            if (word.getValue().equals("-") && word.getKey().isEmpty()) {
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
        String TTL_FILENAME = String.format("%d.ttl", System.currentTimeMillis());
        OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME);
        RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

        ArrayList<ErrorPart> res = Companion.getInstance().solveHyphen(language.name(), DIR_PATH_TO_TASK, TTL_FILENAME);

        ArrayList<Error> errors = new ArrayList<>();
        errors.add(new Error(res));

        LinkedHashMap<String, String> newStudentAnswer = new LinkedHashMap<>();
        if (res.isEmpty()) {
            for (Map.Entry<String, String> entry : studentAnswer.entrySet()) {
                if (entry.getKey().isEmpty()) {
                    newStudentAnswer.put(String.format("h_%d", System.currentTimeMillis()), tokenToCheck);
                } else {
                    newStudentAnswer.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
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

        if (hypotheses.size() < 1) {
            return new ValidateTokenPositionResult(
                    new ArrayList<>(),
                    new LinkedHashMap<>(),
                    taskInTTLFormat,
                    wordsToSelect
            );
        }

        Resource newWordResource = hypotheses.get(0);
        LinkedHashMap<String, String> newStudentAnswer = new LinkedHashMap<>();
        String hypothesysId = "";

        int i = 0;
        for (Map.Entry<String, String> entry : studentAnswer.entrySet()) {
            if (i > hypotheses.size()) {
                break;
            }
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

        Model hypothesisModel = buildModelFromStudentAnswer(newStudentAnswer, newWordResource);

        // Write the model to a string in TTL format
        String TTL_FILENAME = String.format("%d.ttl", System.currentTimeMillis());
        OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME);
        RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

        ArrayList<ErrorPart> res = Companion.getInstance().solve(language.name(), DIR_PATH_TO_TASK, TTL_FILENAME);

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
        LinkedHashMap<String, String> newestStudentAnswer = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : newStudentAnswer.entrySet()) {
            if (entry.getKey().equals(hypothesysId)) {
                newestStudentAnswer.put("", tokenToCheck);
            } else {
                newestStudentAnswer.put(entry.getKey(), entry.getValue());
            }
        }

        return new ValidateTokenPositionResult(
                errors,
                newestStudentAnswer,
                taskInTTLFormat,
                wordsToSelect
        );
    }

    private ValidateTokenPositionResult validateFinish() throws FileNotFoundException {
        Model hypothesisModel = buildFinishModel(studentAnswer);

        // Write the model to a string in TTL format
        String TTL_FILENAME = String.format("%d.ttl", System.currentTimeMillis());
        OutputStream out = new FileOutputStream(DIR_PATH_TO_TASK + TTL_FILENAME);
        RDFDataMgr.write(out, hypothesisModel, Lang.TURTLE);

        LinkedHashMap<String, String> leftAdjectivesToPlaceHyphenWithParents = Companion.getInstance().solveFinish(
                language.name(),
                DIR_PATH_TO_TASK,
                TTL_FILENAME
        );
        ArrayList<Error> errors = new ArrayList<>();
        ArrayList<ErrorPart> errorParts = new ArrayList<>();

        boolean nextShouldBeHyphen = false;
        String leftAdj = "";
        String leftAdjParent = "";
        for (Map.Entry<String, String> entry : studentAnswer.entrySet()) {
            if (nextShouldBeHyphen) {
                if (!entry.getValue().equals("-")) {
                    errorParts.add(new ErrorPart(FINISH_ERR.get(language).get(0), "text"));
                    errorParts.add(new ErrorPart(leftAdj, "lexeme"));
                    errorParts.add(new ErrorPart(FINISH_ERR.get(language).get(1), "text"));
                    errorParts.add(new ErrorPart(leftAdjParent, "lexeme"));
                }
                nextShouldBeHyphen = false;
            }
            if (leftAdjectivesToPlaceHyphenWithParents.containsKey(entry.getValue())) {
                leftAdj = entry.getValue();
                leftAdjParent = leftAdjectivesToPlaceHyphenWithParents.get(entry.getValue());
                nextShouldBeHyphen = true;
            }
        }

        errors.add(new Error(errorParts));

        return new ValidateTokenPositionResult(
                errors,
                studentAnswer,
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

    private Model buildFinishModel(LinkedHashMap<String, String> studentAnswer) {
        Model hypothesisModel = model;

        // для всех гипотез проверить на ошибки
        Property x = model.createProperty(NAMESPACE + "var...");

        Resource lastElement = null;
        for (Map.Entry<String, String> word : studentAnswer.entrySet()) {
            lastElement = hypothesisModel.getResource(NAMESPACE + word.getKey());
        }

        hypothesisModel.add(lastElement, x, "X");

        return hypothesisModel;
    }
}
