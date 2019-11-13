/*
 *
 * Copyright (c) 2006-2019, Speedment, Inc. All Rights Reserved.
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
module com.speedment.runtime.connector.mysql {
    exports com.speedment.runtime.connector.mysql;
    exports com.speedment.runtime.connector.mysql.provider;
    exports com.speedment.runtime.connector.mysql.internal to com.speedment.runtime.connector.mariadb;

    requires transitive com.speedment.common.injector;
    requires transitive com.speedment.runtime.core;
    requires transitive com.speedment.runtime.config;
    requires transitive com.speedment.runtime.field;
}