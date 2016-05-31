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

import com.datastax.driver.mapping.annotations.*;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MapperInvalidAnnotationsTest {

    MappingManager mappingManager = mock(MappingManager.class);

    static class Invalid1 {
    }

    @Table(name = "foo")
    @UDT(name = "foo")
    static class Invalid2 {
    }

    @Table(name = "foo")
    @Accessor
    static class Invalid3 {
    }

    @UDT(name = "foo")
    @Accessor
    static class Invalid4 {
    }

    interface Invalid5 {
    }

    @Table(name = "foo")
    @Accessor
    interface Invalid6 {
    }

    @UDT(name = "foo")
    @Accessor
    interface Invalid7 {
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "@Table annotation was not found on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid1")
    public void should_throw_IAE_when_Table_annotation_not_found_on_entity_class() throws Exception {
        AnnotationParser.parseEntity(Invalid1.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @Table and @UDT on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid2")
    public void should_throw_IAE_when_UDT_annotation_found_on_entity_class() throws Exception {
        AnnotationParser.parseEntity(Invalid2.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @Table and @Accessor on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid3")
    public void should_throw_IAE_when_Accessor_annotation_found_on_entity_class() throws Exception {
        AnnotationParser.parseEntity(Invalid3.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "@UDT annotation was not found on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid1")
    public void should_throw_IAE_when_UDT_annotation_not_found_on_udt_class() throws Exception {
        AnnotationParser.parseUDT(Invalid1.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @UDT and @Table on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid2")
    public void should_throw_IAE_when_Table_annotation_found_on_udt_class() throws Exception {
        AnnotationParser.parseUDT(Invalid2.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @UDT and @Accessor on class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid4")
    public void should_throw_IAE_when_Accessor_annotation_found_on_udt_class() throws Exception {
        AnnotationParser.parseUDT(Invalid4.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "@Accessor annotation was not found on interface " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid5")
    public void should_throw_IAE_when_Accessor_annotation_not_found_on_accessor_class() throws Exception {
        AnnotationParser.parseAccessor(Invalid5.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @Accessor and @Table on interface " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid6")
    public void should_throw_IAE_when_Table_annotation_found_on_accessor_class() throws Exception {
        AnnotationParser.parseAccessor(Invalid6.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Cannot have both @Accessor and @UDT on interface " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid7")
    public void should_throw_IAE_when_UDT_annotation_found_on_accessor_class() throws Exception {
        AnnotationParser.parseAccessor(Invalid7.class, mappingManager);
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "@Accessor annotation is only allowed on interfaces, got class " +
                            "com.datastax.driver.mapping.MapperInvalidAnnotationsTest\\$Invalid4")
    public void should_throw_IAE_when_Accessor_annotation_found_on_concrete_class() throws Exception {
        AnnotationParser.parseAccessor(Invalid4.class, mappingManager);
    }

    @Table(name = "foo", keyspace = "ks")
    static class Invalid8 {
        @Field
        int invalid;
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Annotation @Field is not allowed on property 'invalid'")
    public void should_not_allow_Field_on_entity_class() throws Exception {
        AnnotationParser.parseEntity(Invalid8.class, mappingManager);
    }

    @UDT(name = "foo", keyspace = "ks")
    static class Invalid9 {

        int invalid;

        @Column
        public int getInvalid() {
            return invalid;
        }
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Annotation @Column is not allowed on property 'invalid'")
    public void should_not_allow_Column_on_udt_class() throws Exception {
        AnnotationParser.parseUDT(Invalid9.class, mappingManager);
    }

    @Table(name = "foo", keyspace = "ks")
    static class Invalid10 {

        @PartitionKey
        @ClusteringColumn
        int invalid;

    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Property 'invalid' cannot be annotated with both @PartitionKey and @ClusteringColumn")
    public void should_not_allow_PartitionKey_and_ClusteringColumn_on_same_property() throws Exception {
        AnnotationParser.parseEntity(Invalid10.class, mappingManager);
    }

    @Table(name = "foo", keyspace = "ks")
    static class Invalid11 {

        @Computed("foo")
        @Column
        int invalid;

    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Property 'invalid' cannot be annotated with both @Column and @Computed")
    public void should_not_allow_Computed_and_Column_on_same_property() throws Exception {
        AnnotationParser.parseEntity(Invalid11.class, mappingManager);
    }

    @Table(name = "foo", keyspace = "ks")
    static class Invalid12 {

        @Computed("")
        int invalid;

    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Property 'invalid': attribute 'value' of annotation @Computed is mandatory for computed properties")
    public void should_not_allow_Computed_with_empty_value() throws Exception {
        AnnotationParser.parseEntity(Invalid12.class, mappingManager);
    }

    @Table(name = "foo", keyspace = "ks")
    static class Invalid13 {

        @PartitionKey(-1)
        int invalid;

    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp =
                    "Invalid ordering value -1 for annotation @PartitionKey of property 'invalid', was expecting 0")
    public void should_not_allow_PartitionKey_with_wrong_order() throws Exception {
        AnnotationParser.parseEntity(Invalid13.class, mappingManager);
    }

}
