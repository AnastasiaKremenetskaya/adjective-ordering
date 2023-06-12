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
        new DomainModel(
                new ClassesDictionary(),
                new DecisionTreeVarsDictionary(),
                new EnumsDictionary(),
                new PropertiesDictionary(),
                new RelationshipsDictionary(),
                "src/main/resources/input_examples_adj/"
        );
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
        assertEquals("velvet", res.getErrors().get(0).getError(2).getText());
    }

    @Test
    public void testError2WrongDependentWordOrder() throws IOException {
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
        assertEquals(", так как", res.getErrors().get(0).getError(3).getText());
        assertEquals("salt", res.getErrors().get(0).getError(4).getText());
        assertEquals("является словом, зависимым от", res.getErrors().get(0).getError(5).getText());
        assertEquals("cod", res.getErrors().get(0).getError(6).getText());
    }

    @Test
    public void testError1WrongHypernymOrder() throws IOException {
        String task = "@prefix ns1: <http://www.vstu.ru/poas/code#> .\n@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\nns1:item_0 a ns1:ADJ ;\n           rdfs:label \"Japanese\" ;\n           ns1:hasHypernym ns1:Origin ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_1 a ns1:ADJ  ;\n           rdfs:label \"salt\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_2 .\n\nns1:item_2 a ns1:ADJ ;\n           rdfs:label \"cod\" ;\n           ns1:hasHypernym ns1:Material  ;\n           ns1:isChild ns1:item_3 .\n\nns1:item_3 a ns1:NOUN ;\n           rdfs:label \"sellers\" .";
        ArrayList<String> wordsToSelect = new ArrayList<>();
        wordsToSelect.add("salt");

        LinkedHashMap<String, String> studentAnswerMap = new LinkedHashMap<>();
        studentAnswerMap.put("", "salt");
        studentAnswerMap.put("item_0", "Japanese");
        studentAnswerMap.put("item_2", "cod");
        studentAnswerMap.put("item_3", "sellers");

        ValidateTokenPositionResult res = validator.checkTokenPosition(lang, task, studentAnswerMap, "salt", wordsToSelect);
        assertEquals("Japanese", res.getErrors().get(0).getError(15).getText());
        assertEquals(", описывающее", res.getErrors().get(0).getError(16).getText());
        assertEquals("Национальность", res.getErrors().get(0).getError(17).getText());
        assertEquals(", должно находиться перед прилагательным", res.getErrors().get(0).getError(18).getText());
        assertEquals("cod", res.getErrors().get(0).getError(19).getText());
        assertEquals(", описывающим", res.getErrors().get(0).getError(20).getText());
        assertEquals("Материал", res.getErrors().get(0).getError(21).getText());
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
}