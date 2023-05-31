package com.grpc.domain

import its.model.DomainModel
import its.reasoner.LearningSituation
import its.reasoner.nodes.DecisionTreeReasoner._static.getAnswer
import its.reasoner.nodes.DecisionTreeReasoner._static.getTrace
import org.apache.jena.vocabulary.RDFS
import responses.Error
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList


class Solver(
    language: String,
    DIR_PATH_TO_TASK: String
) {
    private val lang = language
    private var model: LearningSituation = LearningSituation("$DIR_PATH_TO_TASK$TTL_FILENAME.ttl")

    fun solve(): ArrayList<Error> {
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
        var res = ArrayList<Error>()
        if (errorQuestion == isXLeftToY && yVar != null) {
            res = getParenthesisOrderingError(xVar, yVar, errorExplanation)
        } else if (errorQuestion == isYLeftToX && yVar != null) {
            res = getParenthesisOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == areHypernymsOrdered && yVar != null) {
            res = getHypernymOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == isHyphenCorrect) {
            res.add(Error(errorQuestion, "text"))
        }

        return res
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
        errorQuestion: ArrayList<String>
    ): ArrayList<Error> {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)
        val xNodeHypernym = getNodeHypernym(decisionTreeVarX)
        val yNodeHypernym = getNodeHypernym(decisionTreeVarY)

        var errors = ArrayList<Error>()
        errors.add(Error(xNodeLabel, "lexem"))
        errors.add(Error(errorQuestion[0], "text"))
        errors.add(Error(yNodeLabel, "lexem"))
        errors.add(Error(errorQuestion[1], "text"))
        errors.add(Error(xNodeHypernym, "text"))
        errors.add(Error(errorQuestion[2], "text"))
        errors.add(Error(yNodeHypernym, "text"))

        return errors
    }

    private fun getParenthesisOrderingError(
        decisionTreeVarX: String,
        decisionTreeVarY: String,
        errorQuestion: ArrayList<String>
    ): ArrayList<Error> {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)

        var errors = ArrayList<Error>()
        errors.add(Error(xNodeLabel, "lexem"))
        errors.add(Error(errorQuestion[0], "text"))
        errors.add(Error(yNodeLabel, "lexem"))
        errors.add(Error(errorQuestion[1], "text"))

        return errors
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
            areHypernymsOrdered to arrayListOf(
                "должно находиться перед",
                "так как прилагательное,описывающее",
                "должно находиться перед прилагательным",
                "описывающим"
            ),
            isYLeftToX to arrayListOf(
                "должно находиться перед",
                "так как слово, являющееся дочерним, должно находиться левее"
            ),
            isXLeftToY to arrayListOf(
                "должно находиться перед",
                "так как слово, являющееся дочерним, должно находиться левее"
            ),
            isHyphenCorrect to arrayListOf("Дефис не должен стоять между выбранными словами, т.к. они не являются частями сложного прилагательного")
        )

        private val ERRORS_EXPLANATION_EN = mapOf(
            areHypernymsOrdered to arrayListOf(
                "should precede",
                "because adjective that describes",
                "should precede adjective that describes"
            ),
            isYLeftToX to arrayListOf(
                "should precede",
                "because child word should be left to parent word"
            ),
            isXLeftToY to arrayListOf(
                "should precede",
                "because child word should be left to parent word"
            ),
            isHyphenCorrect to arrayListOf("Дефис не должен стоять между выбранными словами, т.к. они не являются частями сложного прилагательного")
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
