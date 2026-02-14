/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.webservice.server.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.exception.InvalidDataException;

@Named
public class WSValidationManager {

    private final List<String> additionalFieldsToExcludeFromValidation = new ArrayList<>();

    private final List<String> fieldsToExcludeFromValidation = new ArrayList<>();

    @PostConstruct
    public void addFieldsToExcludeFromValidation() {
        // Do not validate all properties of the AbstractBaseEntity class and above since the values are set after validation.
        for (Field field : AbstractBaseEntity.class.getDeclaredFields()) {
            getFieldsToExcludeFromValidation().add(field.getName());
        }
    }

    public List<String> getAdditionalFieldsToExcludeFromValidation() {
        return additionalFieldsToExcludeFromValidation;
    }

    private List<String> getFieldsToExcludeFromValidation() {
        return fieldsToExcludeFromValidation;
    }

    public <T> void isValid(T entity) throws Exception {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<T>> errors = validator.validate(entity);
        validatorFactory.close();
        throwBeanValidationErrors(errors);
    }

    private <T> void throwBeanValidationErrors(Set<ConstraintViolation<T>> errors) throws Exception {
        for (ConstraintViolation<T> error : errors) {
            if (!getFieldsToExcludeFromValidation().contains(error.getPropertyPath().toString()) && !getAdditionalFieldsToExcludeFromValidation().contains(error.getPropertyPath().toString())) {
                throw new InvalidDataException(error.getPropertyPath() + " " + error.getMessage());
            }
        }
        getAdditionalFieldsToExcludeFromValidation().clear();
    }
}
