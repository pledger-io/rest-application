package com.jongsoft.finance.jackson;

import com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping;
import com.jongsoft.finance.rest.process.VariableMap;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

@MicronautTest
@DisplayName("Variable map serializer")
class VariableMapSerializerTest {

    public static final String JSON = "{\"variables\":{\"number\":{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":123},\"boolean\":{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":true},\"string\":{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":\"value\"},\"list\":{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$VariableList\",\"content\":[{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":\"one\"},{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":\"two\"},{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$WrappedVariable\",\"value\":\"three\"}]}}}";
    public static final String JSON_WITH_EXTRACTION_MAPPINGS = "{\"variables\":{\"mappings\":{\"_type\":\"com.jongsoft.finance.rest.process.VariableMap$VariableList\",\"content\":[{\"_type\":\"com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping\",\"name\":\"account 1\",\"accountId\":123},{\"_type\":\"com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping\",\"name\":\"account 2\"}]}}}";

    @Test
    @DisplayName("Deserialize variable map with simple values")
    void deserialize(ObjectMapper objectMapper) throws IOException {
        var value = objectMapper.readValue(JSON, VariableMap.class);

        Assertions.assertThat(value)
                .isNotNull();
        Assertions.assertThat((String)value.get("string")).isEqualTo("value");
        Assertions.assertThat((int)value.get("number")).isEqualTo(123);
        Assertions.assertThat((boolean)value.get("boolean")).isTrue();
        Assertions.assertThat((List<String>)value.get("list")).containsExactly("one", "two", "three");
    }

    @Test
    @DisplayName("Serialize variable map with simple values")
    void serialize(ObjectMapper objectMapper) throws IOException {
        var variables = new VariableMap();
        variables.put("string", "value");
        variables.put("number", 123);
        variables.put("boolean", true);
        variables.put("list", List.of("one", "two", "three"));

        var json = objectMapper.writeValueAsString(variables);

        Assertions.assertThat(json).isEqualTo(JSON);
    }

    @Test
    @DisplayName("Deserialize variable map with extraction mappings")
    void deserializeVariables(ObjectMapper objectMapper) throws IOException {
        var value = objectMapper.readValue(JSON_WITH_EXTRACTION_MAPPINGS, VariableMap.class);

        Assertions.assertThat(value)
                .isNotNull();
        Assertions.assertThat((List<ExtractionMapping>)value.get("mappings")).containsExactly(
                new ExtractionMapping("account 1", 123L),
                new ExtractionMapping("account 2", null));
    }

    @Test
    @DisplayName("Serialize variable map with extraction mappings")
    void serializeVariables(ObjectMapper objectMapper) throws IOException {
        var variables = new VariableMap();
        variables.put("mappings", List.of(
                new ExtractionMapping("account 1", 123L),
                new ExtractionMapping("account 2", null)));

        var json = objectMapper.writeValueAsString(variables);

        Assertions.assertThat(json).isEqualTo(JSON_WITH_EXTRACTION_MAPPINGS);
    }
}