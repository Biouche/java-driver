/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.mapping;

import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.mapping.annotations.*;
import com.google.common.reflect.TypeToken;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Maps a Java bean property to a table column or a UDT field.
 * <p>
 * Properties can be either accessed through getter and setter pairs,
 * or by direct field access, depending on what is available in the
 * entity/UDT class.
 */
class PropertyMapper {

    private final String propertyName;
    final String alias;
    final String columnName;
    final TypeToken<Object> javaType;
    final TypeCodec<Object> customCodec;

    private final Field field;
    private final Method getter;
    private final Method setter;
    private final int position;
    private final Map<Class<? extends Annotation>, Annotation> annotations;

    PropertyMapper(String propertyName, String alias, Field field, PropertyDescriptor property) {
        this.propertyName = propertyName;
        this.alias = alias;
        this.field = field;
        getter = ReflectionUtils.findGetter(property);
        setter = ReflectionUtils.findSetter(property);
        annotations = ReflectionUtils.scanPropertyAnnotations(field, property);
        columnName = inferColumnName();
        position = inferPosition();
        javaType = inferJavaType(property);
        customCodec = createCustomCodec();
    }

    Object getValue(Object entity) {
        try {
            // try getter first, if available, otherwise direct field access
            if (getter != null)
                return getter.invoke(entity);
            else
                return field.get(entity);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read property '" + propertyName + "' in " + entity.getClass().getName(), e);
        }
    }

    void setValue(Object entity, Object value) {
        try {
            // try setter first, if available, otherwise direct field access
            if (setter != null)
                setter.invoke(entity, value);
            else
                field.set(entity, value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write property '" + propertyName + "' in " + entity.getClass().getName(), e);
        }
    }

    boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return annotations.containsKey(annotationClass);
    }

    Collection<Annotation> getAnnotations() {
        return annotations.values();
    }

    @SuppressWarnings("unchecked")
    <A extends Annotation> A annotation(Class<A> annotationClass) {
        return (A) annotations.get(annotationClass);
    }

    boolean isComputed() {
        return hasAnnotation(Computed.class);
    }

    boolean isTransient() {
        return hasAnnotation(Transient.class);
    }

    boolean isPartitionKey() {
        return hasAnnotation(PartitionKey.class);
    }

    boolean isClusteringColumn() {
        return hasAnnotation(ClusteringColumn.class);
    }

    int getPosition() {
        return position;
    }

    private String inferColumnName() {
        Column column = annotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return Metadata.quote(column.caseSensitive() ? column.name() : column.name().toLowerCase());
        }
        com.datastax.driver.mapping.annotations.Field udtField = annotation(com.datastax.driver.mapping.annotations.Field.class);
        if (udtField != null && !udtField.name().isEmpty()) {
            return Metadata.quote(udtField.caseSensitive() ? udtField.name() : udtField.name().toLowerCase());
        }
        if (isComputed()) {
            return annotation(Computed.class).value();
        }
        return Metadata.quote(propertyName.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private TypeToken<Object> inferJavaType(PropertyDescriptor property) {
        Type type;
        if (getter != null)
            type = getter.getGenericReturnType();
        else if (field != null)
            type = field.getGenericType();
        else
            // this will not work for generic types
            type = property.getPropertyType();
        return (TypeToken<Object>) TypeToken.of(type);
    }

    private int inferPosition() {
        if (isPartitionKey()) {
            return annotation(PartitionKey.class).value();
        }
        if (isClusteringColumn()) {
            return annotation(ClusteringColumn.class).value();
        }
        return -1;
    }

    private TypeCodec<Object> createCustomCodec() {
        Class<? extends TypeCodec<?>> codecClass = getCustomCodecClass();
        if (codecClass.equals(Defaults.NoCodec.class))
            return null;
        @SuppressWarnings("unchecked")
        TypeCodec<Object> instance = (TypeCodec<Object>) ReflectionUtils.newInstance(codecClass);
        return instance;
    }

    private Class<? extends TypeCodec<?>> getCustomCodecClass() {
        Column column = annotation(Column.class);
        if (column != null)
            return column.codec();
        com.datastax.driver.mapping.annotations.Field udtField = annotation(com.datastax.driver.mapping.annotations.Field.class);
        if (udtField != null)
            return udtField.codec();
        return Defaults.NoCodec.class;
    }

    @Override
    public String toString() {
        return propertyName;
    }

}
