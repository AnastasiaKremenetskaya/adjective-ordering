package com.grpc.domain

import its.model.DomainModel
import its.reasoner.LearningSituation
import its.reasoner.nodes.DecisionTreeReasoner._static.getAnswer
import its.reasoner.nodes.DecisionTreeReasoner._static.getResults
import org.apache.jena.vocabulary.RDFS
import responses.ErrorPart


class Solver(
) {
    private var lang = "RU"
    private lateinit var model: LearningSituation

    fun solve(
        language: String,
        DIR_PATH_TO_TASK: String
    ): ArrayList<ErrorPart> {
        this.lang = language
        this.model = LearningSituation("$DIR_PATH_TO_TASK$TTL_FILENAME.ttl")

        //решение задачи - от наиболее краткого ответа до наиболее подробного - выбрать одно из трех
        val answer = DomainModel.decisionTree.main.getAnswer(model) //Получить тру/фолс ответ
        if (answer) {
            return arrayListOf()
        }

        val trace =
            DomainModel.decisionTree.main.getResults(model) //Получить посещенные узлы по всему дереву - в порядке полного вычисления

        var i: Int = trace.size - 2
        var errorQuestion = trace.get(i).node.additionalInfo["error_type"].toString()
        if (errorQuestion.equals("null")) {
            i = trace.size
            while (i-- > 0) {
                errorQuestion = trace.get(i).node.additionalInfo["error_type"].toString()
                if (!errorQuestion.equals("null")) {
                    break
                }
            }
        }

        if (errorQuestion.equals(error_1)) {
            return getError1(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["X_"],
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["Y_"],
                trace.get(i).variablesSnapshot["z"],
                ERRORS_EXPLANATION[lang]?.get(error_1),
            )
        }
        if (errorQuestion.equals(error_2)) {
            return getError2_3(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["Y"],
                ERRORS_EXPLANATION[lang]?.get(error_2),
            )
        }
        if (errorQuestion.equals(error_3)) {
            return getError2_3(
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["X"],
                ERRORS_EXPLANATION[lang]?.get(error_3),
            )
        }
        if (errorQuestion.equals(error_4)) {
            return getError4(
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["info_z"],
                ERRORS_EXPLANATION[lang]?.get(error_4),
            )
        }

        return ArrayList<ErrorPart>()
    }

    fun solveHyphen(
        language: String,
        DIR_PATH_TO_TASK: String
    ): ArrayList<ErrorPart> {
        this.lang = language
        this.model = LearningSituation("$DIR_PATH_TO_TASK$TTL_FILENAME.ttl")

        //решение задачи - от наиболее краткого ответа до наиболее подробного - выбрать одно из трех
        val answer = DomainModel.decisionTree("hyph").main.getAnswer(model) //Получить тру/фолс ответ
        val trace =
            DomainModel.decisionTree.main.getResults(model) //Получить посещенные узлы по всему дереву - в порядке полного вычисления

        if (answer) {
            return arrayListOf()
        }

        var errorQuestion = ""
        var errorExplanation: ArrayList<String>? = null

        var i: Int = trace.size
        while (i-- > 0) {
            errorQuestion = trace.get(i).node.additionalInfo["label"].toString()
            errorExplanation = ERRORS_EXPLANATION[lang]?.get(errorQuestion)
            if (!errorExplanation.isNullOrEmpty()) {
                break
            }
        }

        val xVar = model.decisionTreeVariables["X"]
        val yVar = model.decisionTreeVariables["Y"]
        if (xVar == null || errorExplanation == null) {
            return arrayListOf()
        }
        var res = ArrayList<ErrorPart>()
        if (errorQuestion == isXLeftToY && yVar != null) {
            res = getParenthesisOrderingError(xVar, yVar, errorExplanation)
        } else if (errorQuestion == isYLeftToX && yVar != null) {
            res = getParenthesisOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == areHypernymsOrdered && yVar != null) {
            res = getHypernymOrderingError(yVar, xVar, errorExplanation)
        } else if (errorQuestion == isHyphenCorrect) {
            res.add(ErrorPart(errorExplanation[0], "text"))
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
    ): ArrayList<ErrorPart> {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)
        val xNodeHypernym = getNodeHypernym(decisionTreeVarX)
        val yNodeHypernym = getNodeHypernym(decisionTreeVarY)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(xNodeHypernym, "text"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(yNodeHypernym, "text"))

        return errorParts
    }

    private fun getError1(
        X: String?,
        X_: String?,
        Y: String?,
        Y_: String?,
        Z: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || X_.isNullOrEmpty() || Y.isNullOrEmpty() ||
            Y_.isNullOrEmpty() || Z.isNullOrEmpty() || errorQuestion.isNullOrEmpty()
        ) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)
        val x_NodeLabel = getNodeLabel(X_)
        val y_NodeLabel = getNodeLabel(Y_)
        val zNodeLabel = getNodeLabel(Z)
        val xNodeHypernym = getNodeHypernym(X_)
        val yNodeHypernym = getNodeHypernym(Y_)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(x_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(y_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[3], "text"))
        errorParts.add(ErrorPart(x_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text"))
        errorParts.add(ErrorPart(y_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[5], "text"))
        errorParts.add(ErrorPart(zNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[6], "text"))
        errorParts.add(ErrorPart(x_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[7], "text"))
        errorParts.add(ErrorPart(xNodeHypernym, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[8], "text"))
        errorParts.add(ErrorPart(y_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[9], "text"))
        errorParts.add(ErrorPart(yNodeHypernym, "lexeme"))

        return errorParts
    }

    private fun getError2_3(
        X: String?,
        Y: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || Y.isNullOrEmpty() || errorQuestion.isNullOrEmpty()
        ) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))

        return errorParts
    }

    private fun getError4(
        X: String?,
        Y: String?,
        Z: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || Y.isNullOrEmpty() || Z.isNullOrEmpty()
            || errorQuestion.isNullOrEmpty()) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)
        val zNodeLabel = getNodeLabel(Z)
        val xNodeHypernym = getNodeHypernym(X)
        val yNodeHypernym = getNodeHypernym(Y)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(zNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[3], "text"))
        errorParts.add(ErrorPart(xNodeHypernym, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[5], "text"))
        errorParts.add(ErrorPart(yNodeHypernym, "lexeme"))

        return errorParts
    }

    private fun getParenthesisOrderingError(
        decisionTreeVarX: String,
        decisionTreeVarY: String,
        errorQuestion: ArrayList<String>
    ): ArrayList<ErrorPart> {
        val xNodeLabel = getNodeLabel(decisionTreeVarX)
        val yNodeLabel = getNodeLabel(decisionTreeVarY)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))

        return errorParts
    }

    companion object {
        @Volatile
        private var instance: Solver? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Solver().also { instance = it }
            }

        const val TTL_FILENAME =
            "task"
        const val NAMESPACE =
            "http://www.vstu.ru/poas/code#"

        private var areHypernymsOrdered = "В одинаковом ли порядке расположены токены X и Y -- и гиперонимы X' и Y'?"
        private var isXLeftToY = "Токен X слева от токена Y?"
        private var isYLeftToX = "Токен Y слева от токена X?"
        private var isHyphenCorrect = "Токен слева от X ребенок токена справа от X? Справа от Х не корень?"

        private var error_1 = "error_1"
        private var error_2 = "error_2"
        private var error_3 = "error_3"
        private var error_4 = "error_4"

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
            isHyphenCorrect to arrayListOf("Дефис не должен стоять между выбранными словами, т.к. они не являются частями сложного прилагательного"),

            error_1 to arrayListOf(
                "является частью сложного прилагательного со словом",
                ",",
                "является частью сложного прилагательного с главным словом",
                ", при этом",
                "и",
                "имеют общее главное слово",
                "и должны располагаться (вместе со всеми зависимыми словами) в порядке своих категорий:  прилагательное",
                "описывающее",
                "должно находиться перед прилагательным",
                "описывающим"
            ),
            error_2 to arrayListOf(
                "должен стоять перед",
                ", так как",
                "является словом, зависимым от",
            ),
            error_3 to arrayListOf(
                "должен стоять перед",
                ", так как",
                "является словом, зависимым от",
            ),
            error_4 to arrayListOf(
                "должно находиться перед",
                ", так как они оба являются прилагательными, относящимися к одному главному слову",
                ", а прилагательное",
                ", описывающее",
                "должно находиться перед прилагательным",
                ", описывающим"
            )
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
            isHyphenCorrect to arrayListOf("Дефис не должен стоять между выбранными словами, т.к. они не являются частями сложного прилагательного"),
            error_1 to arrayListOf(
                "является частью сложного прилагательного со словом",
                ",",
                "является частью сложного прилагательного с главным словом",
                ", при этом",
                "и",
                "имеют общее главное слово",
                "и должны располагаться (вместе со всеми зависимыми словами) в порядке своих категорий:  прилагательное",
                ", описывающее",
                "должно находиться перед прилагательным",
                ", описывающим"
            ),
            error_2 to arrayListOf(
                "should precede",
                "because",
                "is dependent from",
            ),
            error_3 to arrayListOf(
                "should precede",
                "because",
                "is dependent from",
            ),
            error_4 to arrayListOf(
                "должно находиться перед",
                ", так как они оба являются прилагательными, относящимися к одному главному слову",
                ", а прилагательное",
                ", описывающее",
                "должно находиться перед прилагательным",
                ", описывающим"
            )
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
