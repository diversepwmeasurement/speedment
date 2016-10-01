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
package com.speedment.runtime.config.identifier;

import com.speedment.runtime.config.identifier.trait.HasDbmsName;
import com.speedment.runtime.config.identifier.trait.HasSchemaName;
import com.speedment.runtime.config.identifier.trait.HasTableName;
import com.speedment.runtime.config.internal.identifier.TableIdentifierImpl;
import com.speedment.runtime.config.util.DocumentDbUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Identifies a particular Table. The identifier is an immutable non-complex
 * object that only contains the names of the nodes required to uniquely
 * identify it in the database.
 * <p>
 * To find the actual documents referred to by the identifier, the following
 * utility methods can be used:
 * <ul>
 *
 * <li>DocumentDbUtil#referencedTable(Project, Project, TableIdentifier)
 * <li>DocumentDbUtil#referencedSchema(Project, Project, TableIdentifier)
 * <li>DocumentDbUtil#referencedDbms(Project, TableIdentifier)
 * </ul>
 *
 * @param <ENTITY> the entity type
 *
 * @author Emil Forslund
 * @since 2.3
 * @see DocumentDbUtil
 */
public interface TableIdentifier<ENTITY> extends
    HasDbmsName,
    HasSchemaName,
    HasTableName {

    static class Hidden {

        private static final Map<TableIdentifier, TableIdentifier> INTERNED = new ConcurrentHashMap<>();

    }

    static <ENTITY> TableIdentifier<ENTITY> of(String dbmsName, String schemaName, String tableName) {
        final TableIdentifier<ENTITY> newTableIdentity = new TableIdentifierImpl<>(dbmsName, schemaName, tableName);
        Hidden.INTERNED.putIfAbsent(newTableIdentity, newTableIdentity);
        return Hidden.INTERNED.get(newTableIdentity);
    }

}
