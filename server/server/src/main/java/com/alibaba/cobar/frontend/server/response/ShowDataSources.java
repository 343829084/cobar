/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.alibaba.cobar.frontend.server.response;

import java.nio.ByteBuffer;

import com.alibaba.cobar.config.CobarConfig;
import com.alibaba.cobar.config.DataNodesConfig;
import com.alibaba.cobar.config.DataSourcesConfig;
import com.alibaba.cobar.config.InstancesConfig;
import com.alibaba.cobar.config.MachinesConfig;
import com.alibaba.cobar.defs.Fields;
import com.alibaba.cobar.frontend.server.ServerConnection;
import com.alibaba.cobar.net.packet.EOFPacket;
import com.alibaba.cobar.net.packet.FieldPacket;
import com.alibaba.cobar.net.packet.ResultSetHeaderPacket;
import com.alibaba.cobar.net.packet.RowDataPacket;
import com.alibaba.cobar.startup.CobarServer;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.PacketUtil;
import com.alibaba.cobar.util.StringUtil;

/**
 * 查询有效数据节点的当前数据源
 * 
 * @author xianmao.hexm
 */
public class ShowDataSources {

    private static final int FIELD_COUNT = 4;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c);

        // write field
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c);
        }

        // write eof
        buffer = eof.write(buffer, c);

        // write rows
        byte packetId = eof.packetId;
        DataNodesConfig dataNodes = CobarServer.getInstance().getConfig().getDataNodes();
        for (DataNodesConfig.DataNode node : dataNodes.getDataNodes().values()) {
            RowDataPacket row = getRow(node, c.getCharset());
            row.packetId = ++packetId;
            buffer = row.write(buffer, c);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c);

        // post write
        c.write(buffer);
    }

    private static RowDataPacket getRow(DataNodesConfig.DataNode node, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(node.getName(), charset));
        String ds = node.getDataSources()[0];
        if (ds != null) {
            CobarConfig cc = CobarServer.getInstance().getConfig();
            DataSourcesConfig.DataSource dsc = cc.getDataSources().getDataSource(ds);
            InstancesConfig.Instance ici = cc.getInstances().getInstance(dsc.getInstance());
            MachinesConfig.Machine mcm = cc.getMachines().getMachine(ici.getMachine());
            row.add(StringUtil.encode(mcm.getHost(), charset));
            row.add(IntegerUtil.toBytes(ici.getPort()));
            row.add(StringUtil.encode(dsc.getSchema(), charset));
        } else {
            row.add(null);
            row.add(null);
            row.add(null);
        }
        return row;
    }

}
