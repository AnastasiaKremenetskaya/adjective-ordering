import com.gpch.grpc.protobuf.Language;
import com.grpc.domain.ValidateTokenPosition;
import com.grpc.responses.ValidateTokenPositionResult;
import its.model.DomainModel;
import its.model.dictionaries.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValidateTokenPositionTest {

    static ValidateTokenPosition validator;
    static Language lang;

    @BeforeAll
    static void init() {
        new DomainModel(new ClassesDictionary(), new DecisionTreeVarsDictionary(), new EnumsDictionary(), new PropertiesDictionary(), new RelationshipsDictionary(), "src/main/resources/input_examples_adj/");
        lang = Language.RU;
        validator = ValidateTokenPosition.getInstance();
    }

    @Test
    public void testError4WrongHypernymPrecedes() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:DET ;\n    rdfs:label \"a\" ;\n    ns1:isChild ns1:item_4 .\n\nns1:item_1 a ns1:ADJ ;\n    rdfs:label \"comfortable\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_4 .\n\nns1:item_2 a ns1:ADJ ;\n    rdfs:label \"new\" ;\n    ns1:hasHypernym ns1:Age ;\n    ns1:isChild ns1:item_4 .\n\nns1:item_3 a ns1:ADJ ;\n    rdfs:label \"velvet\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild ns1:item_4 .\n\nns1:item_4 a ns1:NOUN ;\n    rdfs:label \"dress\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("comfortable");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("item_0", "a");
        studentAnswerMap.put("item_2", "new");
        studentAnswerMap.put("item_3", "velvet");
        studentAnswerMap.put("", "comfortable");
        studentAnswerMap.put("item_4", "dress");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "comfortable", wordsToSelect);
        assertEquals("comfortable", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("new", res.getErrors().get(0).getError(2).getText());
    }

    @Test
    public void testError8WrongDependentWordOrder() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"salt\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("salt");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("item_0", "Japanese");
        studentAnswerMap.put("item_2", "cod");
        studentAnswerMap.put("", "salt");
        studentAnswerMap.put("item_3", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "salt", wordsToSelect);
        assertEquals("salt", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
        assertEquals("потому что части сложного прилагательного должны идти подряд", res.getErrors().get(0).getError(3).getText());
    }

    // мб это описание ошибки даже лучше
//    @Test
//    public void testError2WrongDependentWordOrder() throws IOException {
//        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"salt\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
//        ArrayList<String> wordsToSelect = new ArrayList<>();
//        wordsToSelect.add("salt");
//
//        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
//        studentAnswerMap.put("item_0", "Japanese");
//        studentAnswerMap.put("item_2", "cod");
//        studentAnswerMap.put("", "salt");
//        studentAnswerMap.put("item_3", "sellers");
//
//        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "salt", wordsToSelect);
//        assertEquals("salt", res.getErrors().get(0).getError(0).getText());
//        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
//        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
//        assertEquals(", так как", res.getErrors().get(0).getError(3).getText());
//        assertEquals("salt", res.getErrors().get(0).getError(4).getText());
//        assertEquals("является словом, зависимым от", res.getErrors().get(0).getError(5).getText());
//        assertEquals("cod", res.getErrors().get(0).getError(6).getText());
//    }
//
    @Test
    public void testError8BreakCompoundWrongDependent() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"black\" ;\n           ns1:hasHypernym ns1:Colour  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("black");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "black");
        studentAnswerMap.put("item_0", "Japanese");
        studentAnswerMap.put("item_2", "cod");
        studentAnswerMap.put("item_3", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "black", wordsToSelect);
        assertEquals("black", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
        assertEquals("потому что части сложного прилагательного должны идти подряд", res.getErrors().get(0).getError(3).getText());
    }

    @Test
    public void testError8BreakCompoundWrongMain() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"salt\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("cod");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "cod");
        studentAnswerMap.put("item_0", "Japanese");
        studentAnswerMap.put("item_1", "salt");
        studentAnswerMap.put("item_3", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "cod", wordsToSelect);
        assertEquals("Japanese", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
    }


    @Test
    public void testError1WrongHypernymOrder() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"salt\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("salt");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "salt");
        studentAnswerMap.put("item_0", "Japanese");
        studentAnswerMap.put("item_3", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "salt", wordsToSelect);
        assertEquals("salt", res.getErrors().get(0).getError(0).getText());
        assertEquals("является частью сложного прилагательного с главным словом", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
        assertEquals("Japanese", res.getErrors().get(0).getError(11).getText());
        assertEquals(", описывающее", res.getErrors().get(0).getError(12).getText());
        assertEquals("Национальность", res.getErrors().get(0).getError(13).getText());
        assertEquals(", должно находиться перед прилагательным", res.getErrors().get(0).getError(14).getText());
        assertEquals("cod", res.getErrors().get(0).getError(15).getText());
        assertEquals(", описывающим", res.getErrors().get(0).getError(16).getText());
        assertEquals("Материал", res.getErrors().get(0).getError(17).getText());
    }

    @Test
    public void testError4WrongHypernymOrder2() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n    rdfs:label \"nice\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_1 a ns1:ADJ ;\n    rdfs:label \"mash\" ;\n    ns1:hasHypernym ns1:PhysicalQuality ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:NOUN ;\n    rdfs:label \"soup\" .\n";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("mash");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "mash");
        studentAnswerMap.put("item_0", "nice");
        studentAnswerMap.put("item_2", "soup");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "mash", wordsToSelect);
        assertEquals("nice", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("mash", res.getErrors().get(0).getError(2).getText());
        assertEquals(", так как они оба являются прилагательными, относящимися к одному главному слову", res.getErrors().get(0).getError(3).getText());
        assertEquals("Мнение", res.getErrors().get(0).getError(8).getText());
        assertEquals("Физические свойства", res.getErrors().get(0).getError(12).getText());
    }

    @Test
    public void testError7HyphBetweenDifferendCompound() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n    rdfs:label \"amazingly\" ;\n        ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_3 a ns1:ADJ ;\n    rdfs:label \"salt\" ;\n            ns1:hasHypernym ns1:Material ;\nns1:isChild ns1:item_5 .\n\nns1:item_2 a ns1:ADJ ;\n    rdfs:label \"smart\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_5 a ns1:ADJ ;\n    rdfs:label \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_6 a ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("-");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("item_0", "amazingly");
        studentAnswerMap.put("item_2", "smart");
        studentAnswerMap.put("", "-");
        studentAnswerMap.put("item_3", "salt");
        studentAnswerMap.put("item_5", "cod");
        studentAnswerMap.put("item_6", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "-", wordsToSelect);
        assertEquals("Дефисы ставятся только между частями одного сложного прилагательного. В данном случае", res.getErrors().get(0).getError(0).getText());
        assertEquals("smart", res.getErrors().get(0).getError(1).getText());
        assertEquals("является частью сложного прилагательного с главным словом", res.getErrors().get(0).getError(2).getText());
        assertEquals("sellers", res.getErrors().get(0).getError(3).getText());
        assertEquals(", а", res.getErrors().get(0).getError(4).getText());
        assertEquals("salt", res.getErrors().get(0).getError(5).getText());
        assertEquals("- это прилагательное с главным словом", res.getErrors().get(0).getError(6).getText());
        assertEquals("cod", res.getErrors().get(0).getError(7).getText());
    }

    @Test
    public void testError1WrongMainWord() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n    rdfs:label \"amazingly\" ;\n        ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_3 a ns1:ADJ ;\n    rdfs:label \"salt\" ;\n            ns1:hasHypernym ns1:Material ;\nns1:isChild ns1:item_5 .\n\nns1:item_2 a ns1:ADJ ;\n    rdfs:label \"smart\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_5 a ns1:ADJ ;\n    rdfs:label \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_6 a ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("cod");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "cod");
        studentAnswerMap.put("item_0", "amazingly");
        studentAnswerMap.put("item_2", "smart");
        studentAnswerMap.put("item_3", "salt");
        studentAnswerMap.put("item_6", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "cod", wordsToSelect);
        assertEquals("amazingly", res.getErrors().get(0).getError(0).getText());
        assertEquals("является частью сложного прилагательного с главным словом", res.getErrors().get(0).getError(1).getText());
        assertEquals("smart", res.getErrors().get(0).getError(2).getText());
        assertEquals("smart", res.getErrors().get(0).getError(11).getText());
        assertEquals(", должно находиться перед прилагательным", res.getErrors().get(0).getError(14).getText());
        assertEquals("cod", res.getErrors().get(0).getError(15).getText());
    }

    @Test
    public void testError8() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n    rdfs:label \"amazingly\" ;\n        ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_3 a ns1:ADJ ;\n    rdfs:label \"salt\" ;\n            ns1:hasHypernym ns1:Material ;\nns1:isChild ns1:item_5 .\n\nns1:item_2 a ns1:ADJ ;\n    rdfs:label \"smart\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_5 a ns1:ADJ ;\n    rdfs:label \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild ns1:item_6 .\n\nns1:item_6 a ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("salt");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "salt");
        studentAnswerMap.put("item_0", "amazingly");
        studentAnswerMap.put("item_2", "smart");
        studentAnswerMap.put("item_5", "cod");
        studentAnswerMap.put("item_6", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "salt", wordsToSelect);
        assertEquals("salt", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
    }

    // smart amazing-salt-cod sellers
    @Test
    public void testError4MultipleAdjectivesInCompound() throws IOException {
        String task = "@prefix ns1:  <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0\n    a               ns1:ADJ ;\n    rdfs:label      \"smart\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_1\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_2 .\n\nns1:item_2\n    a               ns1:ADJ ;\n    rdfs:label      \"salt\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_3 .\n\nns1:item_3\n    a               ns1:ADJ ;\n    rdfs:label      \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_4\n    a          ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("cod");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "cod");
        studentAnswerMap.put("item_0", "smart");
        studentAnswerMap.put("item_1", "amazing");
        studentAnswerMap.put("item_2", "salt");
        studentAnswerMap.put("item_4", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "cod", wordsToSelect);
        assertEquals("smart", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
        assertEquals(", так как они оба являются прилагательными, относящимися к одному главному слову", res.getErrors().get(0).getError(3).getText());
        assertEquals("sellers", res.getErrors().get(0).getError(4).getText());
    }

    // smart amazing-salt-cod sellers
    @Test
    public void testError1MultipleAdjectivesInCompound() throws IOException {
        String task = "@prefix ns1:  <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0\n    a               ns1:ADJ ;\n    rdfs:label      \"smart\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_1\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_2 .\n\nns1:item_2\n    a               ns1:ADJ ;\n    rdfs:label      \"salt\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_3 .\n\nns1:item_3\n    a               ns1:ADJ ;\n    rdfs:label      \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_4\n    a          ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("amazing");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "amazing");
        studentAnswerMap.put("item_0", "smart");
        studentAnswerMap.put("item_4", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "amazing", wordsToSelect);
        assertEquals("amazing", res.getErrors().get(0).getError(0).getText());
        assertEquals("является частью сложного прилагательного с главным словом", res.getErrors().get(0).getError(1).getText());
        assertEquals("cod", res.getErrors().get(0).getError(2).getText());
        assertEquals("smart", res.getErrors().get(0).getError(5).getText());
        assertEquals("cod", res.getErrors().get(0).getError(7).getText());
        assertEquals("имеют общее главное слово", res.getErrors().get(0).getError(8).getText());
        assertEquals("sellers", res.getErrors().get(0).getError(9).getText());
        assertEquals("smart", res.getErrors().get(0).getError(11).getText());
        assertEquals(", должно находиться перед прилагательным", res.getErrors().get(0).getError(14).getText());
        assertEquals("cod", res.getErrors().get(0).getError(15).getText());
    }

    // the beautiful books
    @Test
    public void testError2WrongDeterminer() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\n# det wrong order\n\nns1:item_0 a ns1:ADJ ;\n    rdfs:label \"beautiful\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_1 a ns1:DET ;\n    rdfs:label \"The\" ;\n    ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:NOUN ;\n    rdfs:label \"books\" .\n    ";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("The");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();

        studentAnswerMap.put("item_2", "books");
        studentAnswerMap.put("", "The");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "The", wordsToSelect);
        assertEquals("The", res.getErrors().get(0).getError(0).getText());
        assertEquals("должно находиться перед", res.getErrors().get(0).getError(1).getText());
        assertEquals("books", res.getErrors().get(0).getError(2).getText());
    }

    // amazing big-amazing-salt-cod sellers
    @Test
    public void testCorrectRepeatingWordPrePosition() throws IOException {
        String task = "@prefix ns1:  <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_5\n    a               ns1:ADJ ;\n    rdfs:label      \"big\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_1 .\n\nns1:item_1\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_2 .\n\nns1:item_2\n    a               ns1:ADJ ;\n    rdfs:label      \"salt\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_3 .\n\nns1:item_3\n    a               ns1:ADJ ;\n    rdfs:label      \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_4\n    a          ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("amazing");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "amazing");
        studentAnswerMap.put("item_1", "big");
        studentAnswerMap.put("item_5", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "amazing", wordsToSelect);
        assertEquals(res.getErrors().size(), 0);
    }

    // amazing big-amazing-salt-cod sellers
    @Test
    public void testCorrectRepeatingWordPostPosition() throws IOException {
        String task = "@prefix ns1:  <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_5 .\n\nns1:item_1\n    a               ns1:ADJ ;\n    rdfs:label      \"big\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_2 .\n\nns1:item_2\n    a               ns1:ADJ ;\n    rdfs:label      \"amazing\" ;\n    ns1:hasHypernym ns1:Opinion ;\n    ns1:isChild     ns1:item_3 .\n\nns1:item_3\n    a               ns1:ADJ ;\n    rdfs:label      \"salt\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_4 .\n\nns1:item_4\n    a               ns1:ADJ ;\n    rdfs:label      \"cod\" ;\n    ns1:hasHypernym ns1:Material ;\n    ns1:isChild     ns1:item_5 .\n\nns1:item_5\n    a          ns1:NOUN ;\n    rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("amazing");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("item_1", "big");
        studentAnswerMap.put("", "amazing");
        studentAnswerMap.put("item_5", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "amazing", wordsToSelect);
        assertEquals(res.getErrors().size(), 0);
    }
}
