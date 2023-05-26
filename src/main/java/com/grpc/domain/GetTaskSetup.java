package com.grpc.domain;

import com.grpc.responses.GetTaskSetupResult;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import java.util.LinkedHashMap;

import static com.grpc.domain.Solver.NAMESPACE;

public class GetTaskSetup {
    public GetTaskSetupResult getTask(String taskInTTLFormat)  {
        Model model  = ModelFactory.createDefaultModel().read(IOUtils.toInputStream(taskInTTLFormat, "UTF-8"), null, "TTL");

        LinkedHashMap<String, String> tokens = new LinkedHashMap<>();

        // Iterate over the statements in the model
        StmtIterator iter = model.listStatements();

        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property isChildProperty = model.createProperty(NAMESPACE + "isChild");

            tokens.put(subject.getProperty(RDFS.label).getString(), "");

            // If the statement has no ns1:isChild property - it's root
            if (!subject.hasProperty(isChildProperty)) {
                tokens.put(subject.getProperty(RDFS.label).getString(), subject.getLocalName());
            }
        }


        return new GetTaskSetupResult("", tokens);
    }
}
