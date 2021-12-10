/*
 * Copyright (c) 2021, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.naming.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common constants.
 *
 * @author cvarela
 * @since 0.4
 */
public class CommonConstants {

    public static final String PRIVATE_KEY_FILENAME = "privateKey";
    public static final String GLOBAL_PRIVATE_KEY_FILENAME = "globalPrivateKey";
    public static final String DRIVER_KEY = "driverClassName";
    public static final String URL_KEY = "url";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String ON_CONNECT_SCRIPT_KEY = "onConnectScript";
    public static final String DISABLE_POOL_KEY = "disablePool";
    public static final String DISABLE_ATOMIKOS_POOL_KEY = DISABLE_POOL_KEY + ".atomikos";
    public static final String DISABLE_DBCP_POOL_KEY = DISABLE_POOL_KEY + ".dbcp";
    public static final String TYPE_KEY = "type";
    public static final String TYPE_DATASOURCE = "javax.sql.DataSource";

    public static final List<String> RESERVED_NAMES;

    static {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, DISABLE_POOL_KEY, DISABLE_ATOMIKOS_POOL_KEY, DISABLE_DBCP_POOL_KEY,
                DRIVER_KEY, GLOBAL_PRIVATE_KEY_FILENAME, TYPE_KEY, TYPE_DATASOURCE, ON_CONNECT_SCRIPT_KEY, PASSWORD_KEY,
                PRIVATE_KEY_FILENAME, URL_KEY, USERNAME_KEY);
        RESERVED_NAMES = Collections.unmodifiableList(list);
    }
}
