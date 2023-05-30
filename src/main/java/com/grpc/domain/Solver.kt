package com.grpc.domain

import its.model.DomainModel
import its.reasoner.LearningSituation
import its.reasoner.nodes.DecisionTreeReasoner._static.getAnswer
import its.reasoner.nodes.DecisionTreeReasoner._static.getTrace
import org.apache.jena.vocabulary.RDFS
import java.io.FileInputStream
import java.util.*


class Solver(
    language: String,
    DIR_PATH_TO_TASK: String
) {
    private val lang = language
    private var model: LearningSituation = LearningSituation("$DIR_PATH_TO_TASK$TTL_FILENAME.ttl")

    fun solve(): ArrayList<String> {
        //решение задачи - от наиболее краткого ответа до наиболее подробного - выбрать одно из трех
        val answer = DomainModel.decisionTree.main.getAnswer(model) //Получить тру/фолс ответ
        val trace =
            DomainModel.decisionTree.main.getTrace(model) //Получить посещенные узлы по всему дереву - в порядке полного вычисления

        if (answer) {
            return arrayListOf()
        }

        var errorQuestion = trace[trace.size - 3].additionalInfo["label"].toString()
        var traceStep = 3
        if (errorQuestion == "null") {
            traceStep = 2
            errorQuestion = trace[trace.size - traceStep].additionalInfo["label"].toString()
        }
        val errorExplanation =
            ERRORS_EXPLANATION[lang]?.get(trace[trace.size - traceStep].additionalInfo["label"].toString())

        val xVar = model.decisionTreeVariables["X"]
        val yVar = model.decisionTreeVariables["Y"]
        if (xVar == null || errorExplanation == null) {
            return arrayListOf()
        }
        var res = ""
        if (errorQuestion == isXLeftToY && yVar != null) {
            res = getParenthesisOrderingError(xVar, yVar, errorExplanation)
        } else if (errorQuestion == isYLeftToX && yVar != null) {
            res = getParenthesisOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == areHypernymsOrdered && yVar != null) {
            res = getHypernymOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == isHyphenCorrect) {
            res = errorExplanation
        }

        return arrayListOf(res)
    }

    private fun getNodeLabel(decisionTreeVar: String): String {
        return model.model.getResource(NAMESPACE + decisionTreeVar).asResource().getProperty(RDFS.label).string
    }

    private fun getNodeHypernym(decisionTreeVar: String): String {
        val hasHypernym = model.model.getProperty(NAMESPACE + "hasHypernym")
        val iter = model.model.getResource(NAMESPACE + decisionTreeVar).listProperties(hasHypernym)
        while (iter.hasNext()) {
            val stmt = iter.nextStatement()
            return HYPERNYMS[lang]?.get(stmt.resource.localName) ?: ""
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
        const val TTL_FILENAME =
            "task"
        const val NAMESPACE =
            "http://www.vstu.ru/poas/code#"

        private var areHypernymsOrdered = "В одинаковом ли порядке расположены токены X и Y -- и гиперонимы X' и Y'?"
        private var isXLeftToY = "Токен X слева от токена Y?"
        private var isYLeftToX = "Токен Y слева от токена X?"
        private var isHyphenCorrect = "Токен слева от X ребенок токена справа от X? Справа от Х не корень?"

        private val ERRORS_EXPLANATION_RU = mapOf(
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
            isHyphenCorrect to "Дефис не должен стоять между словами %s и %s, т.к. они не являются частями (одного) сложного прилагательного"
        )

        private val ERRORS_EXPLANATION_EN = mapOf(
            areHypernymsOrdered to "%s should precede %s, \n" +
                    "because adjective that describes %s,\n" +
                    "should precede adjective that describes %s",
            isYLeftToX to "%s should precede %s, \n" +
                    "because child word should be left to parent word",
            isXLeftToY to "%s should precede %s, \n" +
                    "because child word should be left to parent word",
            isHyphenCorrect to "Дефис не должен стоять между словами, т.к. они не являются частями сложного прилагательного"
        )

        private val ERRORS_EXPLANATION = mapOf(
            "RU" to ERRORS_EXPLANATION_RU,
            "EN" to ERRORS_EXPLANATION_EN,
        )

        private val HYPERNYMS_RU = mapOf(
            "Opinion" to "Мнение",
            "Size" to "Размер",
            "Age" to "Возраст",
            "Shape" to "Форма",
            "Colour" to "Цвет",
            "Origin" to "Национальность",
            "Material" to "Материал",
            "Purpose" to "Цель",
        )

        private val HYPERNYMS_EN = mapOf(
            "Opinion" to "Opinion",
            "Size" to "Size",
            "Age" to "Age",
            "Shape" to "Shape",
            "Colour" to "Colour",
            "Origin" to "Origin",
            "Material" to "Material",
            "Purpose" to "Purpose",
        )

        private val HYPERNYMS = mapOf(
            "RU" to HYPERNYMS_RU,
            "EN" to HYPERNYMS_EN,
        )
    }
}
