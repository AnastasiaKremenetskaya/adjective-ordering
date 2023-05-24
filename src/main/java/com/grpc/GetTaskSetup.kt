package com.grpc

import com.grpc.Solver.Companion.NAMESPACE
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDFS


class GetTaskSetup() {
    fun getTask(taskInTTLFormat: String): GetTaskSetupResult {
        val model: Model = ModelFactory.createDefaultModel().read(taskInTTLFormat.byteInputStream(Charsets.UTF_8), null, "TTL")

        val tokens = mutableMapOf<String, String>()

        // Iterate over the statements in the model
        val iter = model.listStatements()

        while (iter.hasNext()) {
            val stmt = iter.nextStatement()
            val subject = stmt.subject
            val isChildProperty = model.createProperty(NAMESPACE + "isChild")

            tokens[subject.getProperty(RDFS.label).string] = ""

            // If the statement has no ns1:isChild property - it's root
            if (!subject.hasProperty(isChildProperty)) {
                subject.localName
                tokens[subject.getProperty(RDFS.label).string] = subject.localName
            }
        }


        return GetTaskSetupResult("", tokens)
    }
}
