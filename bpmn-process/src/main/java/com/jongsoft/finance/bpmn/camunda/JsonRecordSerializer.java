package com.jongsoft.finance.bpmn.camunda;

import io.micronaut.serde.ObjectMapper;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractTypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.type.ObjectTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonRecordSerializer<T> extends AbstractTypedValueSerializer<TypedValue> {
    private static final Logger logger = LoggerFactory.getLogger(JsonRecordSerializer.class);

    final ObjectMapper objectMapper;
    final Class<T> supportedClass;

    public JsonRecordSerializer(ObjectMapper objectMapper, Class<T> supportedClass) {
        super(new ObjectTypeImpl());
        this.objectMapper = objectMapper;
        this.supportedClass = supportedClass;
    }

    @Override
    public String getName() {
        return supportedClass.getName();
    }

    @Override
    public String getSerializationDataformat() {
        return getName();
    }

    @Override
    public TypedValue convertToTypedValue(UntypedValueImpl untypedValue) {
        logger.trace("Converting untyped value to typed value: {}", untypedValue.getValue().getClass().getSimpleName());

        try {
            var jsonString = objectMapper.writeValueAsString(untypedValue.getValue());
            return Variables.serializedObjectValue(jsonString)
                    .objectTypeName(supportedClass.getName())
                    .serializationDataFormat("application/json")
                    .create();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize ImportJobSettings", e);
        }
    }

    @Override
    public void writeValue(TypedValue typedValue, ValueFields valueFields) {
        ObjectValue objectValue = (ObjectValue) typedValue;
        valueFields.setByteArrayValue(objectValue.getValueSerialized().getBytes());
    }

    @Override
    public TypedValue readValue(ValueFields valueFields, boolean b, boolean b1) {
        logger.trace("Reading value from value fields: {}", valueFields.getName());
        try {
            return Variables.objectValue(objectMapper.readValue(
                            new String(valueFields.getByteArrayValue()),
                            supportedClass))
                    .serializationDataFormat("application/json")
                    .create();
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize ImportJobSettings", e);
        }
    }

    @Override
    public boolean canHandle(TypedValue value) {
        if (value instanceof ObjectValue objectValue) {
            return supportedClass.getName().equals(objectValue.getObjectTypeName());
        }

        return canWriteValue(value);
    }

    @Override
    protected boolean canWriteValue(TypedValue typedValue) {
        if (typedValue instanceof UntypedValueImpl) {
            logger.trace("Checking if un-typed value can be written: {}", typedValue.getValue().getClass().getSimpleName());
            return supportedClass.isInstance(typedValue.getValue());
        }

        return false;
    }
}
