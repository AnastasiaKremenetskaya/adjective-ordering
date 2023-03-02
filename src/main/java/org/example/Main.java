package org.example;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class Main {
    static Path path = Paths.get(".").toAbsolutePath().normalize();
    static String data = path.toFile().getAbsolutePath() +
            "/src/main/resources/python/model.xml";

    static String rules = path.toFile().getAbsolutePath() +
            "/src/main/resources/rule.rules";

    public static void main(String[] args) {
        // Load RDF data
        Model model = ModelFactory.createDefaultModel().read(data);
        model.read(data);

        // Load rules
        Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL(rules));

        // Запускаем Reasoner на модели и выводим результаты
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        ValidityReport validity = infModel.validate();
        if (validity.isValid()) {
            System.out.println("OK");
            boolean result = infModel.contains(null, null, ResourceFactory.createResource("rule1"));
            System.out.println(result);
        } else {
            System.out.println("Conflicts");
            for (Iterator<ValidityReport.Report> i = validity.getReports(); i.hasNext(); ) {
                System.out.println(" - " + i.next());
            }
        }
    }
}
