package org.example;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    static Path path = Paths.get(".").toAbsolutePath().normalize();

    public static void main(String[] args) {
        // Load RDF data
        String data = path.toFile().getAbsolutePath() +
                "/src/main/resources/python/model.xml";
        Model model = ModelFactory.createDefaultModel().read(data);
        model.read(data);

        // Load rules
        String rules = path.toFile().getAbsolutePath() +
                "/src/main/resources/rule.rules";
        Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(rules));

        // Запускаем Reasoner на модели и выводим результаты
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        if (infModel.validate().isValid()) {
            boolean result = infModel.contains(null, null, ResourceFactory.createResource());
            System.out.println(result);

        }
    }
}