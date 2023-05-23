package com.grpc;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdjectiveOrderChecker {
    static final ImmutableMap<String, String> ADJ_DESCRIPTIONS = ImmutableMap.<String, String>builder()
            .put("ADJOpinion", "Мнение")
            .put("ADJSize", "Размер")
            .put("ADJAge", "Возраст")
            .put("ADJShape", "Форма")
            .put("ADJColour", "Цвет")
            .put("ADJOrigin", "Национальность")
            .put("ADJMaterial", "Материал")
            .put("ADJPurpose", "Цель")
            .build();

    static Path path = Paths.get(".").toAbsolutePath().normalize();
    static String rules = path.toFile().getAbsolutePath() +
            "/src/main/resources/rule.rules";

    public static OrderCheckerResult getAdjectiveOrderingErrors(String RDFinXMLFormat) {
        List<String> adjectiveOrderingErrors = new ArrayList<String>();
        List<String> modelValidationErrors = new ArrayList<String>();

        // Load RDF data
        Model model = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(RDFinXMLFormat, "UTF-8"), null);

        // Load rules
        Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(rules));

        // Запускаем Reasoner на модели и выводим результаты
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        ValidityReport validity = infModel.validate();
        if (validity.isValid()) {
            // Get all the resources in the model that have an "ns1:incorrectOrder" property
            Iterator<Resource> incorrectOrderResources = infModel.listSubjectsWithProperty(
                    infModel.getProperty("http://example.com/incorrectOrder"));

            // Print out the subjects and objects of the "ns1:incorrectOrder" properties
            while (incorrectOrderResources.hasNext()) {
                Resource subject = incorrectOrderResources.next();
                Resource object = subject.getPropertyResourceValue(
                        infModel.getProperty("http://example.com/incorrectOrder"));
                String firstWord = subject.getProperty(RDFS.label).getString();
                String firstWordPOS = ADJ_DESCRIPTIONS.get(subject.getPropertyResourceValue(RDF.type).getLocalName());
                String secondWord = object.getProperty(RDFS.label).getString();
                String secondWordPOS = ADJ_DESCRIPTIONS.get(object.getPropertyResourceValue(RDF.type).getLocalName());
                String result = secondWord + " должно находиться перед " + firstWord;
                result += ", так как прилагательное, описывающее " + secondWordPOS;
                result += ", должно находиться перед прилагательным, описывающим " + firstWordPOS;

                adjectiveOrderingErrors.add(result);
            }
        } else {
            for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); ) {
                adjectiveOrderingErrors.add(i.next().toString());
            }
        }

        OrderCheckerResult res = new OrderCheckerResult(adjectiveOrderingErrors, modelValidationErrors);
        for (String err:res.getAdjectiveOrderingErrors()) {
            System.out.println(err);
        }
        for (String err:res.getModelValidationErrors()) {
            System.out.println(err);
        }
        return res;
    }
}
