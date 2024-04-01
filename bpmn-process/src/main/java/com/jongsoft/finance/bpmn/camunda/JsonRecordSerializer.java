package com.jongsoft.finance.bpmn.camunda;

import io.micronaut.serde.ObjectMapper;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.type.ObjectTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.io.IOException;

public class JsonRecordSerializer<T> extends AbstractTypedValueSerializer<TypedValue> {

    final ObjectMapper objectMapper;
    final Class<T> supportedClass;

    public JsonRecordSerializer(ObjectMapper objectMapper, Class<T> supportedClass) {
        super(new ObjectTypeImpl());
        this.objectMapper = objectMapper;
        this.supportedClass = supportedClass;
    }

    @Override
    public String getName() {
        return "record-json";
    }

    @Override
    public String getSerializationDataformat() {
        return getName();
    }

    @Override
    public TypedValue convertToTypedValue(UntypedValueImpl untypedValue) {
        var importJobSettings = (Record) untypedValue.getValue();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(importJobSettings);
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize ImportJobSettings", e);
        }
        return Variables.serializedObjectValue(jsonString)
                .serializationDataFormat(getName())
                .create();
    }

    @Override
    public void writeValue(TypedValue typedValue, ValueFields valueFields) {
        ObjectValue objectValue = (ObjectValue) typedValue;
        valueFields.setByteArrayValue(objectValue.getValueSerialized().getBytes());
    }

    @Override
    public TypedValue readValue(ValueFields valueFields, boolean b, boolean b1) {
        try {
            return Variables.objectValue(objectMapper.readValue(
                            new String(valueFields.getByteArrayValue()),
                            supportedClass))
                    .serializationDataFormat(getName())
                    .create();
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize ImportJobSettings", e);
        }
    }

    @Override
    protected boolean canWriteValue(TypedValue typedValue) {
        return supportedClass.isAssignableFrom(typedValue.getValue().getClass());
    }
}
