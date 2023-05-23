package com.grpc
import its.model.*
import its.model.dictionaries.*
import its.reasoner.LearningSituation
import its.reasoner.nodes.DecisionTreeReasoner._static.getAnswer
import its.reasoner.nodes.DecisionTreeReasoner._static.getTrace
import org.apache.jena.vocabulary.RDFS

class Solver(
) {
    init {
        DomainModel(
            ClassesDictionary(),
            DecisionTreeVarsDictionary(),
            EnumsDictionary(),
            PropertiesDictionary(),
            RelationshipsDictionary(),
            DIR_PATH_TO_TASK,
        )
    }

    private var model: LearningSituation = LearningSituation("$DIR_PATH_TO_TASK$TTL_FILENAME.ttl")

    private var areHypernymsOrdered = "В одинаковом ли порядке расположены токены X и Y -- и гиперонимы X' и Y'?"
    private var isXLeftToY = "Токен X слева от токена Y?"
    private var isYLeftToX = "Токен Y слева от токена X?"

    private val ERRORS_EXPLANATION = mapOf(
        areHypernymsOrdered to "%s должно находиться перед %s, \n" +
                "так как прилагательное, \n" +
                "описывающее %s,\n" +
                "должно находиться перед прилагательным, \n" +
                "описывающим %s",
        isYLeftToX to "%s должно находиться перед %s, \n" +
                "так как слово, являющееся дочерним,\n" +
                "должно находиться левее",
        isXLeftToY to "%s должно находиться перед %s, \n" +
                "так как слово, являющееся дочерним,\n" +
                "должно находиться левее",
    )

    fun solve() :OrderCheckerResult {
        //решение задачи - от наиболее краткого ответа до наиболее подробного - выбрать одно из трех
        val answer = DomainModel.decisionTree.main.getAnswer(model) //Получить тру/фолс ответ
        val trace =
            DomainModel.decisionTree.main.getTrace(model) //Получить посещенные узлы по всему дереву - в порядке полного вычисления

        if (answer) {
            return OrderCheckerResult(arrayListOf(), arrayListOf())
        }

        val errorQuestion = trace[trace.size - 3].additionalInfo["label"].toString()
//        println(errorQuestion)
        val errorExplanation = ERRORS_EXPLANATION[trace[trace.size - 3].additionalInfo["label"].toString()]

        val xVar = model.decisionTreeVariables["X"]
        val yVar = model.decisionTreeVariables["Y"]
        if (xVar == null || yVar == null || errorExplanation == null) {
            return OrderCheckerResult(arrayListOf(), arrayListOf())
        }
        var res = ""
        if (errorQuestion == isXLeftToY) {
            res = getParenthesisOrderingError(xVar, yVar, errorExplanation)
        } else if (errorQuestion == isYLeftToX) {
            res = getParenthesisOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == areHypernymsOrdered) {
            res = getHypernymOrderingError(yVar, xVar, errorExplanation)
        }

        return OrderCheckerResult(arrayListOf(res), arrayListOf())
    }

    private fun getNodeLabel(decisionTreeVar: String): String {
        return model.model.getResource(NAMESPACE + decisionTreeVar).asResource().getProperty(RDFS.label).string
    }

    private fun getNodeHypernym(decisionTreeVar: String): String {
        val hasHypernym = model.model.getProperty(NAMESPACE + "hasHypernym")
        val iter = model.model.getResource(NAMESPACE + decisionTreeVar).listProperties(hasHypernym)
        while (iter.hasNext()) {
            val stmt = iter.nextStatement()
            return stmt.resource.localName
        }
        return ""
    }

    private fun getHypernymOrderingError(
        decisionTreeVarX: String,
        decisionTreeVarY: String,
        errorQuestion: String
    ): String {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)
        val xNodeHypernym = getNodeHypernym(decisionTreeVarX)
        val yNodeHypernym = getNodeHypernym(decisionTreeVarY)

        return String.format(errorQuestion, xNodeLabel, yNodeLabel, xNodeHypernym, yNodeHypernym)
    }

    private fun getParenthesisOrderingError(
        decisionTreeVarX: String,
        decisionTreeVarY: String,
        errorQuestion: String
    ): String {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)

        return String.format(errorQuestion, xNodeLabel, yNodeLabel)
    }

    companion object {
//        const val DIR_PATH_TO_TASK =
//            "/Users/anterekhova/IdeaProjects/adjective-ordering/src/main/resources/input_examples_adj2/"
        const val DIR_PATH_TO_TASK =
            "src/main/resources/input_examples_adj/"
        const val TTL_FILENAME =
            "task"
        const val NAMESPACE =
            "http://www.vstu.ru/poas/code#"
    }
}
