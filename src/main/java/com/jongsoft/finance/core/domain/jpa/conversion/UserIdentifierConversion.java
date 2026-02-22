package com.jongsoft.finance.core.domain.jpa.conversion;

import com.jongsoft.finance.core.value.UserIdentifier;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;

import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
class UserIdentifierConversion implements TypeConverter<String, UserIdentifier> {

    @Override
    public Optional<UserIdentifier> convert(
            String object, Class<UserIdentifier> targetType, ConversionContext context) {
        return Optional.ofNullable(object).map(UserIdentifier::new);
    }
}
