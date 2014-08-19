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
package com.alibaba.cobar.net.factory;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.util.ByteBufferQueue;

/**
 * @author xianmao.hexm
 */
public abstract class FrontendConnectionFactory {

    protected int socketRecvBuffer = 8 * 1024;
    protected int socketSendBuffer = 16 * 1024;
    protected int writeQueueCapcity = 16;
    protected long idleTimeout;
    protected String charset;

    protected abstract FrontendConnection getConnection(SocketChannel channel);

    public FrontendConnection make(SocketChannel channel) throws IOException {
        Socket socket = channel.socket();
        socket.setReceiveBufferSize(socketRecvBuffer);
        socket.setSendBufferSize(socketSendBuffer);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        FrontendConnection c = getConnection(channel);
        c.setWriteQueue(new ByteBufferQueue(writeQueueCapcity));
        c.setIdleTimeout(idleTimeout);
        c.setCharset(charset);
        return c;
    }

    public void setSocketRecvBuffer(int socketRecvBuffer) {
        this.socketRecvBuffer = socketRecvBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public void setWriteQueueCapcity(int writeQueueCapcity) {
        this.writeQueueCapcity = writeQueueCapcity;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}
