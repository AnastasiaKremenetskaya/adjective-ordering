package com.grpc.domain

import com.gpch.grpc.protobuf.Language
import com.grpc.Solver
import com.grpc.Solver.Companion.DIR_PATH_TO_TASK
import com.grpc.Solver.Companion.NAMESPACE
import com.grpc.responses.ValidateTokenPositionResult
import org.apache.jena.query.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFDataMgr
import java.io.File
import java.io.StringWriter


class ValidateTokenPosition(
    language: Language,
    private val taskInTTLFormat: String,
    private val tokens: MutableMap<String, String>, // key - item_0, value - word
    private val tokenToCheck: String // word
) {
    private var model: Model = ModelFactory.createDefaultModel().read(taskInTTLFormat.byteInputStream(Charsets.UTF_8), null, "TTL")
    private val lang = language

    fun checkTokenPosition(): ValidateTokenPositionResult {
        var hypotheses = arrayOf<Resource>()

        // Получить узел с токеном, который надо проверить
        val queryString = "PREFIX ns1: <$NAMESPACE> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT ?resource " +
                "WHERE { " +
                "   ?resource rdfs:label \"$tokenToCheck\" . " +
                "}"

        // Execute the SPARQL query
        val query = QueryFactory.create(queryString)
        val queryExecution = QueryExecutionFactory.create(query, model)
        val resultSet = queryExecution.execSelect()

        // Process the query results
        while (resultSet.hasNext()) {
            val solution = resultSet.nextSolution()
            val resource = solution.getResource("resource")
            hypotheses += resource
            println("Resource with label $tokenToCheck: ${resource.uri}")
        }

        // Close the query execution
        queryExecution.close()

        // для всех гипотез проверить на ошибки
        val p: Property = model.createProperty(NAMESPACE + "tokenPrecedes")
        val x: Property = model.createProperty(NAMESPACE + "var...")
        var prevToken: Resource? = null
        var currentToken: Resource

//        val errorsCountForHypothesisId = emptyMap<Int, String>()

        hypotheses.forEachIndexed { hypothesesIndex, hypothesis ->
            val hypothesisModel: Model = model
            tokens.forEach { entry ->
                if (entry.key == tokenToCheck) {
                    currentToken = hypothesis
                    hypothesisModel.add(currentToken, x, "X")
                } else {
                    currentToken = hypothesisModel.getResource(NAMESPACE + entry.value)
                }

                if (prevToken != null) {
                    hypothesisModel.add(prevToken, p, currentToken)
                }
                prevToken = currentToken
            }

            // Write the model to a string in TTL format
            val writer = StringWriter()
            RDFDataMgr.write(writer, hypothesisModel, org.apache.jena.riot.RDFFormat.TURTLE)
            val content = writer.toString()

            File("$DIR_PATH_TO_TASK${Solver.TTL_FILENAME}.ttl").writeText(content)

            val res = Solver(lang).solve()
            if (res.isEmpty()) {
                tokens[tokenToCheck] = hypothesis.localName
                return ValidateTokenPositionResult(
                    res,
                    tokens,
                    taskInTTLFormat,
                )
            }
            if (hypothesesIndex == hypotheses.size-1) {
                tokens.remove(tokenToCheck)

                return ValidateTokenPositionResult(
                    res,
                    tokens,
                    taskInTTLFormat,
                )
            }
        }
        return ValidateTokenPositionResult(arrayListOf(), tokens, taskInTTLFormat)
    }
}
