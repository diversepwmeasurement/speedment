/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.internal.core.code;

import com.speedment.Speedment;
import com.speedment.internal.codegen.base.Generator;
import com.speedment.internal.codegen.lang.controller.AutoImports;
import com.speedment.internal.codegen.lang.models.AnnotationUsage;
import com.speedment.internal.codegen.lang.models.ClassOrInterface;
import com.speedment.internal.codegen.lang.models.Constructor;
import com.speedment.internal.codegen.lang.models.Field;
import com.speedment.internal.codegen.lang.models.File;
import com.speedment.internal.codegen.lang.models.Interface;
import com.speedment.internal.codegen.lang.models.Javadoc;
import com.speedment.internal.codegen.lang.models.Type;
import static com.speedment.internal.codegen.lang.models.constants.DefaultAnnotationUsage.GENERATED;
import static com.speedment.internal.codegen.lang.models.constants.DefaultJavadocTag.AUTHOR;
import com.speedment.internal.codegen.lang.models.implementation.FileImpl;
import com.speedment.internal.codegen.lang.models.implementation.JavadocImpl;
import com.speedment.internal.codegen.lang.models.values.TextValue;
import static com.speedment.internal.core.code.DefaultJavaClassTranslator.CopyConstructorMode.SETTER;
import com.speedment.config.db.Column;
import com.speedment.config.db.Dbms;
import com.speedment.config.Document;
import com.speedment.config.db.ForeignKey;
import com.speedment.config.db.Index;
import com.speedment.config.db.Project;
import com.speedment.config.db.Schema;
import com.speedment.config.db.Table;
import com.speedment.config.db.PrimaryKeyColumn;
import com.speedment.config.db.trait.HasEnabled;
import com.speedment.config.db.trait.HasMainInterface;
import com.speedment.config.db.trait.HasName;
import static com.speedment.internal.core.code.entity.EntityImplTranslator.SPEEDMENT_NAME;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author pemi
 * @param <C> ConfigEntity type.
 * @param <J> Java type (Interface or Class) to generate
 */
public abstract class DefaultJavaClassTranslator
        <C extends Document & HasName & HasEnabled & HasMainInterface, J extends ClassOrInterface<J>>
        implements JavaClassTranslator<C> {

    public static final String GETTER_METHOD_PREFIX = "get",
            SETTER_METHOD_PREFIX = "set",
            BUILDER_METHOD_PREFIX = SETTER_METHOD_PREFIX,
            GENERATED_JAVADOC_MESSAGE
            = "\n<p>\nThis Class or Interface has been automatically generated by Speedment.\n"
            + "Any changes made to this Class or Interface will be overwritten.";

    private final C configEntity;
    private final Generator codeGenerator;
    private final Speedment speedment;

    public DefaultJavaClassTranslator(Speedment speedment, Generator codeGenerator, C configEntity) {
        this.speedment = requireNonNull(speedment);
        this.configEntity = requireNonNull(configEntity);
        this.codeGenerator = requireNonNull(codeGenerator);
    }

    @Override
    public C getNode() {
        return configEntity;
    }

    protected Speedment getSpeedment() {
        return speedment;
    }

    protected AnnotationUsage generated() {
        return GENERATED.set(new TextValue("Speedment"));
    }

    protected abstract String getFileName();

    protected abstract J make(File file);

    protected void finializeFile(File file) {
        // Do nothing
    }

    @Override
    public File get() {
        final File file = new FileImpl(baseDirectoryName() + "/" + (isInImplPackage() ? "impl/" : "") + getFileName() + ".java");
        final J item = make(file);
        item.set(getJavaDoc());
        file.add(item);
        finializeFile(file);
        file.call(new AutoImports(getCodeGenerator().getDependencyMgr()));
        return file;
    }

    protected abstract String getJavadocRepresentText();

    protected Javadoc getJavaDoc() {
        return new JavadocImpl(getJavadocRepresentText() + " representing an entity (for example, a row) in the " + getNode().toString() + "." + GENERATED_JAVADOC_MESSAGE)
                .add(AUTHOR.setValue("Speedment"));
    }

    public Generator getCodeGenerator() {
        return codeGenerator;
    }

    protected boolean isInImplPackage() {
        return false;
    }

    protected abstract class Builder<T extends ClassOrInterface<T>> {

        private final String name;
        private final Map<Class<?>, List<BiConsumer<T, ? extends Document>>> map;

        // Special for this case
        private final List<BiConsumer<T, ForeignKey>> foreignKeyReferencesThisTableConsumers;

        public Builder(String name) {
            this.name = requireNonNull(name);
            this.map = new HashMap<>();
            this.foreignKeyReferencesThisTableConsumers = new ArrayList<>();
        }

        public Builder<T> addProjectConsumer(BiConsumer<T, Project> consumer) {
            aquireListAndAdd(Project.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addDbmsConsumer(BiConsumer<T, Dbms> consumer) {
            aquireListAndAdd(Dbms.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addSchemaConsumer(BiConsumer<T, Schema> consumer) {
            aquireListAndAdd(Schema.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addTableConsumer(BiConsumer<T, Table> consumer) {
            aquireListAndAdd(Table.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addColumnConsumer(BiConsumer<T, Column> consumer) {
            aquireListAndAdd(Column.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addIndexConsumer(BiConsumer<T, Index> consumer) {
            aquireListAndAdd(Index.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addForeignKeyConsumer(BiConsumer<T, ForeignKey> consumer) {
            aquireListAndAdd(ForeignKey.class, requireNonNull(consumer));
            return this;
        }

        public Builder<T> addForeignKeyReferencesThisTableConsumer(BiConsumer<T, ForeignKey> consumer) {
            foreignKeyReferencesThisTableConsumers.add(requireNonNull(consumer));
            return this;
        }

        @SuppressWarnings("unchecked")
        protected <C extends Document> void aquireListAndAdd(Class<C> clazz, BiConsumer<T, C> consumer) {
            aquireList(requireNonNull(clazz))
                    .add(requireNonNull((BiConsumer<T, Document>) consumer));
        }

        @SuppressWarnings("unchecked")
        protected <C extends Document> List<BiConsumer<T, C>> aquireList(Class<?> clazz) {
            return (List<BiConsumer<T, C>>) (List<?>) map.computeIfAbsent(requireNonNull(clazz), $ -> new ArrayList<>());
        }

        public <D extends Document & HasMainInterface> void act(T item, D document) {
            aquireList(document.mainInterface())
                    .forEach(c -> c.accept(requireNonNull(item), requireNonNull(document)));
        }

        public abstract T newInstance(String name);

        public T build() {
            final T i = newInstance(name);
            act(i, project());
            act(i, dbms());
            act(i, schema());
            act(i, table());

            Stream.of(
                    PrimaryKeyColumn.class,
                    Index.class,
                    Column.class,
                    ForeignKey.class
            ).forEachOrdered(ifType
                    -> aquireList(ifType)
                    .forEach(actor -> table().children()
                            .filter(HasEnabled::test)
                            .filter(ifType::isInstance)
                            .forEachOrdered(c -> actor.accept(i, c)))
            );

            if (Table.class.equals(getNode().mainInterface())) {
                schema().tables()
                        .filter(HasEnabled::test)
                        .flatMap(t -> t.foreignKeys())
                        .filter(fk -> fk.foreignKeyColumns()
                                //.filter(ForeignKeyColumn::isEnabled)
                                .filter(fkc -> fkc.getForeignTableName().equals(getNode().getName()))
                                .findFirst()
                                .isPresent()
                        )
                        .forEachOrdered(fk
                                -> foreignKeyReferencesThisTableConsumers.forEach(
                                        c -> c.accept(i, fk)
                                )
                        );
            }

            i.add(generated());
            return i;
        }

    }

    protected class ClassBuilder extends Builder<com.speedment.internal.codegen.lang.models.Class> {

        public ClassBuilder(String name) {
            super(name);
        }

        @Override
        public com.speedment.internal.codegen.lang.models.Class newInstance(String name) {
            return com.speedment.internal.codegen.lang.models.Class.of(name);
        }

    }

    protected class InterfaceBuilder extends Builder<Interface> {

        public InterfaceBuilder(String name) {
            super(name);
        }

        @Override
        public Interface newInstance(String name) {
            return Interface.of(name);
        }

    }

    public Field fieldFor(Column c) {
        return Field.of(variableName(c), Type.of(c.findTypeMapper().getJavaType()));
    }

    public Constructor emptyConstructor() {
        return Constructor.of().public_();
    }

    public enum CopyConstructorMode {

        SETTER, FIELD, BUILDER;
    }

    public Constructor copyConstructor(Type type, CopyConstructorMode mode) {
        final Constructor constructor = Constructor.of().public_()
                .add(Field.of(SPEEDMENT_NAME, Type.of(Speedment.class)))
                .add("super(" + SPEEDMENT_NAME + ");")
                .add(Field.of(variableName(), type).final_());

        columns().forEachOrdered(c -> {
            switch (mode) {
                case FIELD: {
                    constructor.add("this." + variableName(c) + " = " + variableName() + "." + GETTER_METHOD_PREFIX + typeName(c) + "();");
                    break;
                }
                case SETTER:
                case BUILDER: {
                    final String setterPrefix = (mode == SETTER)
                            ? SETTER_METHOD_PREFIX : BUILDER_METHOD_PREFIX;

                    if (c.isNullable()) {
                        constructor.add(
                                variableName() + "."
                                + GETTER_METHOD_PREFIX + typeName(c)
                                + "().ifPresent(this::"
                                + setterPrefix + typeName(c)
                                + ");"
                        );
                    } else {
                        constructor.add(
                                setterPrefix + typeName(c)
                                + "(" + variableName()
                                + ".get" + typeName(c)
                                + "());"
                        );
                    }
                    break;
                }
            }
        });

        return constructor;
    }
}
