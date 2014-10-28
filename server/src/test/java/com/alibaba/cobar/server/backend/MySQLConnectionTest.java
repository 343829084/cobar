/*
 * Copyright 1999-2014 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cobar.server.backend;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.model.CobarModel;
import com.alibaba.cobar.server.model.DataSources.DataSource;
import com.alibaba.cobar.server.net.nio.NIOConnector;
import com.alibaba.cobar.server.startup.CobarContainer;

/**
 * @author xianmao.hexm
 */
public class MySQLConnectionTest {

    protected static final Logger LOGGER = Logger.getLogger(MySQLConnectionTest.class);

    public static void main(String[] args) throws IOException {
        // 启动容器
        CobarContainer container = CobarContainer.getInstance();
        container.startup();

        // 创建指定数据源的连接
        NIOConnector connector = container.getConnector();
        CobarModel cm = container.getConfigModel();
        DataSource dataSource = cm.getDataSources().getDataSource("S1");
        MySQLConnectionFactory factory = new MySQLConnectionFactory();

        for (int i = 0; i < 10; i++) {
            MySQLConnection c = (MySQLConnection) factory.make(dataSource);
            connector.postConnect(c);
        }
    }

}
