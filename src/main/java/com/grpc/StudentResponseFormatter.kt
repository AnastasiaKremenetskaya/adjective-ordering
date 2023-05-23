package com.grpc

import com.grpc.Solver.Companion.DIR_PATH_TO_TASK
import com.grpc.Solver.Companion.NAMESPACE
import org.apache.jena.query.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFDataMgr
import java.io.File
import java.io.StringWriter


class StudentResponseFormatter(
    private val numberOfTask: Int,
    private val tokens: Map<String, String>, // key - item_0, value - word
    private val tokenToCheck: String // word
) {
    private var ns1 = "http://www.vstu.ru/poas/code#"
    private var model: Model = RDFDataMgr.loadModel("$DIR_PATH_TO_TASK$numberOfTask.ttl")

    fun checkTokenPosition(): OrderCheckerResult {
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
        val p: Property = model.createProperty(ns1 + "tokenPrecedes")
        val x: Property = model.createProperty(ns1 + "var...")
        var prevToken: Resource? = null
        var currentToken: Resource

        hypotheses.forEach { hypothesis ->
            val hypothesisModel: Model = model
            tokens.forEach { entry ->
                if (entry.value == tokenToCheck) {
                    currentToken = hypothesis
                    hypothesisModel.add(currentToken, x, "X")
                } else {
                    currentToken = hypothesisModel.getResource(NAMESPACE + entry.key)
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

            // TODO return если 0 ошибок, иначе проверить другие гипотезы
            return Solver().solve()
        }
        return OrderCheckerResult(arrayListOf(), arrayListOf())
    }
}
