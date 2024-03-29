package com.grpc.domain

import its.model.DomainModel
import its.reasoner.LearningSituation
import its.reasoner.nodes.DecisionTreeReasoner._static.getAnswer
import its.reasoner.nodes.DecisionTreeReasoner._static.getResults
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import responses.ErrorPart
import java.io.File
import java.util.LinkedHashMap


class Solver(
) {
    private var lang = "RU"
    private lateinit var model: LearningSituation

    fun solve(
        language: String,
        DIR_PATH_TO_TASK: String,
        TTL_FILENAME: String
    ): ArrayList<ErrorPart> {
        val filepath = "$DIR_PATH_TO_TASK$TTL_FILENAME"
        this.lang = language
        this.model = LearningSituation(filepath)
        deleteFile(filepath)

        //решение задачи - от наиболее краткого ответа до наиболее подробного - выбрать одно из трех
        val answer = DomainModel.decisionTree.main.getAnswer(model) //Получить тру/фолс ответ

        val trace =
            DomainModel.decisionTree.main.getResults(model) //Получить посещенные узлы по всему дереву - в порядке полного вычисления

        if (answer) {
            return arrayListOf()
        }

//        var i: Int = trace.size - 1
//        if (trace.size > 2) {
//            i = trace.size - 2
//        }
//
        var errorQuestion = ""
//        if (errorQuestion.equals("null")) {
        var i = -1
        while (i++ < trace.size) {
            errorQuestion = trace.get(i).node.additionalInfo["error_type"].toString()
            if (!errorQuestion.equals("null")) {
                break
            }
        }
//        }

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
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["info_z"],
                ERRORS_EXPLANATION[lang]?.get(error_4),
            )
        }
        if (errorQuestion.equals(error_8)) {
            return getError8(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["x_parent"],
                ERRORS_EXPLANATION[lang]?.get(error_8),
            )
        }

        return ArrayList<ErrorPart>()
    }

    fun solveHyphen(
        language: String,
        DIR_PATH_TO_TASK: String,
        TTL_FILENAME: String
    ): ArrayList<ErrorPart> {
        val filepath = "$DIR_PATH_TO_TASK$TTL_FILENAME"
        this.lang = language
        this.model = LearningSituation(filepath)
        deleteFile(filepath)

        val answer = DomainModel.decisionTree("hyph").main.getAnswer(model)
        if (answer) {
            return arrayListOf()
        }

        val trace = DomainModel.decisionTree("hyph").main.getResults(model)

        val xVar = model.decisionTreeVariables["X"]
        val yVar = model.decisionTreeVariables["Y"]
        if (xVar == null || yVar == null) {
            return arrayListOf()
        }

        var errorQuestion = ""

        var i: Int = trace.size
        while (i-- > 0) {
            errorQuestion = trace.get(i).node.additionalInfo["error_type"].toString()
            if (!errorQuestion.isNullOrEmpty()) {
                break
            }
        }
        if (errorQuestion.equals(error_5)) {
            return getError5(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["Y"],
                ERRORS_EXPLANATION[lang]?.get(error_5),
            )
        }
        if (errorQuestion.equals(error_6)) {
            return getError6(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["x_parent"],
                trace.get(i).variablesSnapshot["y_parent"],
                ERRORS_EXPLANATION[lang]?.get(error_6),
            )
        }
        if (errorQuestion.equals(error_7)) {
            return getError6(
                trace.get(i).variablesSnapshot["X"],
                trace.get(i).variablesSnapshot["Y"],
                trace.get(i).variablesSnapshot["x_parent"],
                trace.get(i).variablesSnapshot["y_parent"],
                ERRORS_EXPLANATION[lang]?.get(error_7),
            )
        }

        return ArrayList<ErrorPart>()
    }

    fun solveFinish(
        language: String,
        DIR_PATH_TO_TASK: String,
        TTL_FILENAME: String
    ): LinkedHashMap<String, String> {
        val filepath = "$DIR_PATH_TO_TASK$TTL_FILENAME"
        this.lang = language
        this.model = LearningSituation(filepath)
        deleteFile(filepath)

        val trace = DomainModel.decisionTree("finish").main.getResults(model)

        val leftAdjectivesToPlaceHyphenWithParents = LinkedHashMap<String, String>()

        var i: Int = trace.size
        while (i-- > 0) {
            val adj = trace.get(i).variablesSnapshot["Y"].toString()
            val adjParent = trace.get(i).variablesSnapshot["y_parent"].toString()
            if (!adj.equals("null") && !adjParent.equals("null")) {
                leftAdjectivesToPlaceHyphenWithParents.put(getNodeLabel(adj), getNodeLabel(adjParent))
            }
        }

        return leftAdjectivesToPlaceHyphenWithParents
    }

    private fun getNodeLabel(decisionTreeVar: String): String {
        return model.model.getResource(NAMESPACE + decisionTreeVar).asResource().getProperty(RDFS.label).string
    }

    private fun getNodeClass(decisionTreeVar: String): String {
        val a = model.model.getResource(NAMESPACE + decisionTreeVar).asResource()
            .getProperty(RDF.type).`object`.asNode().localName
        return POS[lang]?.get(a) ?: ""
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

        var xNodeHypernym = getNodeHypernym(X_)
        var yNodeHypernym = getNodeHypernym(Y_)

        var Checked_X = X
        var Checked_X_ = X_
        var Checked_Y = Y
        var Checked_Y_ = Y_

        if (HYPERNYMS_ORDER[xNodeHypernym]!! > HYPERNYMS_ORDER[yNodeHypernym]!!) {
            Checked_X = Y
            Checked_X_ = Y_
            Checked_Y = X
            Checked_Y_ = X_
            val temp = xNodeHypernym
            xNodeHypernym = yNodeHypernym
            yNodeHypernym = temp
        }

        val xNodeLabel = getNodeLabel(Checked_X)
        val yNodeLabel = getNodeLabel(Checked_Y)
        val x_NodeLabel = getNodeLabel(Checked_X_)
        val y_NodeLabel = getNodeLabel(Checked_Y_)
        xNodeHypernym = HYPERNYMS[lang]?.get(xNodeHypernym) ?: ""
        yNodeHypernym = HYPERNYMS[lang]?.get(yNodeHypernym) ?: ""
        val zNodeLabel = getNodeLabel(Z)

        var errorParts = ArrayList<ErrorPart>()
        var oneDescriptionPresents = false

        if (xNodeLabel != x_NodeLabel) {
            oneDescriptionPresents = true
            errorParts.add(ErrorPart(xNodeLabel, "lexeme")) // X
            errorParts.add(ErrorPart(errorQuestion[0], "text")) // является частью сложного прилагательного с главным словом
            errorParts.add(ErrorPart(x_NodeLabel, "lexeme")) // X_
            errorParts.add(ErrorPart(errorQuestion[1], "text")) // ,
        }
        if (yNodeLabel != y_NodeLabel) {
            oneDescriptionPresents = true
            errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
            errorParts.add(ErrorPart(errorQuestion[2], "text")) // является частью сложного прилагательного с главным словом
            errorParts.add(ErrorPart(y_NodeLabel, "lexeme")) // Y_
            errorParts.add(ErrorPart(errorQuestion[1], "text")) // ,
        }
        if (oneDescriptionPresents) {
            errorParts.add(ErrorPart(errorQuestion[3], "text")) // при этом,
        }
        errorParts.add(ErrorPart(x_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text")) // и
        errorParts.add(ErrorPart(y_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[5], "text")) // имеют общее главное слово
        errorParts.add(ErrorPart(zNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[6], "text")) // и должны располагаться (вместе со всеми зависимыми словами) в порядке своих категорий:  прилагательное
        errorParts.add(ErrorPart(x_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[7], "text")) // , описывающее
        errorParts.add(ErrorPart(xNodeHypernym, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[8], "text")) // , должно находиться перед прилагательным
        errorParts.add(ErrorPart(y_NodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[9], "text"))
        errorParts.add(ErrorPart(yNodeHypernym, "lexeme")) // , описывающим

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
            || errorQuestion.isNullOrEmpty()
        ) {
            return ArrayList<ErrorPart>()
        }

        var xNodeHypernym = getNodeHypernym(X)
        var yNodeHypernym = getNodeHypernym(Y)

        var Checked_X = X
        var Checked_Y = Y

        if (HYPERNYMS_ORDER[xNodeHypernym]!! > HYPERNYMS_ORDER[yNodeHypernym]!!) {
            Checked_X = Y
            Checked_Y = X
            val temp = xNodeHypernym
            xNodeHypernym = yNodeHypernym
            yNodeHypernym = temp
        }

        val xNodeLabel = getNodeLabel(Checked_X)
        val yNodeLabel = getNodeLabel(Checked_Y)
        val zNodeLabel = getNodeLabel(Z)
        xNodeHypernym = HYPERNYMS[lang]?.get(xNodeHypernym) ?: ""
        yNodeHypernym = HYPERNYMS[lang]?.get(yNodeHypernym) ?: ""

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

    private fun getError5(
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
        val xClass = getNodeClass(X)
        val yClass = getNodeClass(Y)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(xClass, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[3], "text"))
        errorParts.add(ErrorPart(yClass, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text"))

        return errorParts
    }

    private fun getError6(
        X: String?,
        Y: String?,
        XParent: String?,
        YParent: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || Y.isNullOrEmpty() ||
            XParent.isNullOrEmpty() || YParent.isNullOrEmpty() || errorQuestion.isNullOrEmpty()
        ) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)
        val XParentNodeLabel = getNodeLabel(XParent)
        val YParentNodeLabel = getNodeLabel(YParent)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(YParentNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[3], "text"))
        errorParts.add(ErrorPart(XParentNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text"))

        return errorParts
    }

    private fun getError7(
        X: String?,
        Y: String?,
        XParent: String?,
        YParent: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || Y.isNullOrEmpty() ||
            XParent.isNullOrEmpty() || YParent.isNullOrEmpty() || errorQuestion.isNullOrEmpty()
        ) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)
        val XParentNodeLabel = getNodeLabel(XParent)
        val YParentNodeLabel = getNodeLabel(YParent)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))
        errorParts.add(ErrorPart(YParentNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[2], "text"))
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[3], "text"))
        errorParts.add(ErrorPart(XParentNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[4], "text"))

        return errorParts
    }

    private fun getError8(
        X: String?,
        Y: String?,
        errorQuestion: ArrayList<String>?
    ): ArrayList<ErrorPart> {
        if (X.isNullOrEmpty() || Y.isNullOrEmpty() || errorQuestion.isNullOrEmpty()) {
            return ArrayList<ErrorPart>()
        }

        val xNodeLabel = getNodeLabel(X)
        val yNodeLabel = getNodeLabel(Y)

        var errorParts = ArrayList<ErrorPart>()
        errorParts.add(ErrorPart(xNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[0], "text"))
        errorParts.add(ErrorPart(yNodeLabel, "lexeme"))
        errorParts.add(ErrorPart(errorQuestion[1], "text"))

        return errorParts
    }

    fun deleteFile(path: String) {
        val file = File(path)

        val result = file.delete()
        if (!result) {
            println("Deletion failed.")
        }
    }

    companion object {
        @Volatile
        private var instance: Solver? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Solver().also { instance = it }
            }

        const val NAMESPACE =
            "http://www.vstu.ru/poas/code#"

        private var error_1 = "error_1"
        private var error_2 = "error_2"
        private var error_3 = "error_3"
        private var error_4 = "error_4"

        private var error_5 = "error_5"
        private var error_6 = "error_6"
        private var error_7 = "error_7"
        private var error_8 = "error_8" // разделение compound adj

        private val ERRORS_EXPLANATION_RU = mapOf(
            error_1 to arrayListOf(
                "является частью сложного прилагательного с главным словом",
                ",",
                "является частью сложного прилагательного с главным словом",
                "при этом",
                "и",
                "имеют общее главное слово",
                "и должны располагаться (вместе со всеми зависимыми словами) в порядке своих категорий:  прилагательное",
                ", описывающее",
                ", должно находиться перед прилагательным",
                ", описывающим"
            ),
            error_2 to arrayListOf(
                "должно находиться перед",
                ", так как",
                "является словом, зависимым от",
            ),
            error_3 to arrayListOf(
                "должно находиться перед",
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
            ),
            error_5 to arrayListOf(
                "Дефисы ставятся только между частями сложных прилагательных. В данном случае",
                "- это ",
                ", а",
                "- это ",
                ", поэтому они не являются частями сложного прилагательного",
            ),
            error_6 to arrayListOf(
                "Дефисы ставятся только между частями сложных прилагательных. В данном случае",
                "- это простое прилагательное с главным словом",
                ", а",
                "- это прилагательное с главным словом",
                ", поэтому они не являются частями сложного прилагательного",
            ),
            error_7 to arrayListOf(
                "Дефисы ставятся только между частями одного сложного прилагательного. В данном случае",
                "является частью сложного прилагательного с главным словом",
                ", а",
                "- это прилагательное с главным словом",
                ", поэтому они не являются частями одного сложного прилагательного",
            ),
            error_8 to arrayListOf(
                "должно находиться перед",
                "потому что части сложного прилагательного должны идти подряд",
            ),
        )

        private val ERRORS_EXPLANATION_EN = mapOf(
            error_1 to arrayListOf(
                "is a part of compound adjective with word",
                ",",
                "is a part of compound adjective with word",
                "so",
                "and",
                "have common word they are related to",
                "and should be placed (along with all dependent words) according to the order of their categories:  adjective",
                "that describes",
                "should precede adjective",
                "that describes",
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
                "should precede",
                ", because they both are adjectives dependent from word",
                "and adjective",
                "that describes",
                "should precede adjective",
                "that describes",
            ),
            error_5 to arrayListOf(
                "Hyphen could be placed only between parts of compound adjectives. In this case",
                "is",
                "and",
                "is",
                "that's why mentioned words are not the parts of compound adjective",
            ),
            error_6 to arrayListOf(
                "Hyphen could be placed only between parts of compound adjectives. In this case",
                "is a non-compound adjective dependent from",
                "and",
                "- is an adjective dependent from",
                "that's why mentioned words are not the parts of compound adjective",
            ),
            error_7 to arrayListOf(
                "Hyphen could be placed only between parts of compound adjectives. In this case",
                "is a part of compound adjective dependent from",
                "and",
                "- is an adjective dependent from",
                "that's why mentioned words are not the parts of similar compound adjective",
            ),
            error_8 to arrayListOf(
                "should precede",
                "because parts of compound adjective should be consecutive",
            ),
        )

        private val ERRORS_EXPLANATION = mapOf(
            "RU" to ERRORS_EXPLANATION_RU,
            "EN" to ERRORS_EXPLANATION_EN,
        )

        private val HYPERNYMS_RU = mapOf(
            "Opinion" to "Мнение",
            "Size" to "Размер",
            "PhysicalQuality" to "Физические свойства",
            "Age" to "Возраст",
            "Shape" to "Форма",
            "Colour" to "Цвет",
            "Origin" to "Национальность",
            "Material" to "Материал",
            "Type" to "Тип",
            "Purpose" to "Цель",
        )

        private val HYPERNYMS_EN = mapOf(
            "Opinion" to "Opinion",
            "Size" to "Size",
            "PhysicalQuality" to "PhysicalQuality",
            "Age" to "Age",
            "Shape" to "Shape",
            "Colour" to "Colour",
            "Origin" to "Origin",
            "Material" to "Material",
            "Type" to "Type",
            "Purpose" to "Purpose",
        )

        private val HYPERNYMS_ORDER = mapOf(
            "Opinion" to 1,
            "Size" to 2,
            "PhysicalQuality" to 3,
            "Age" to 4,
            "Shape" to 5,
            "Colour" to 6,
            "Origin" to 7,
            "Material" to 8,
            "Type" to 9,
            "Purpose" to 10,
        )

        private val POS_RU = mapOf(
            "DET" to "детерминант",
            "NOUN" to "главное существительное",
            "ADJ" to "прилагательное",
        )

        private val POS_EN = mapOf(
            "DET" to "determiner",
            "NOUN" to "root noun",
            "ADJ" to "adjective",
        )

        private val HYPERNYMS = mapOf(
            "RU" to HYPERNYMS_RU,
            "EN" to HYPERNYMS_EN,
        )

        private val POS = mapOf(
            "RU" to POS_RU,
            "EN" to POS_EN,
        )
    }
}
