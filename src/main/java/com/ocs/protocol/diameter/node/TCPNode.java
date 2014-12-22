package com.ocs.protocol.diameter.node;

import java.net.*;
import java.io.*;
import java.util.logging.*;

import com.ocs.protocol.diameter.*;

import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class TCPNode extends NodeImplementation
{
    private Thread node_thread;
    private Selector selector; //多路复用器：实现一个线程可以监听多个socketChannel,通过将socketChannel register到selector
    private ServerSocketChannel serverChannel;
    private boolean please_stop;
    private long shutdown_deadline;
    
    public TCPNode(final Node node, final NodeSettings nodeSettings, final Logger logger) {
        super(node, nodeSettings, logger);
    }
    
    void openIO() throws IOException {
    	//创建selector
        this.selector = Selector.open();
        if (this.settings.port() != 0) {
        	//静态工厂方法，不是真的打开通道，而是创建一个通道 （方法名字本身带有一定欺骗性）
            this.serverChannel = ServerSocketChannel.open(); 
            //socket()方法获得server Socket并绑定到相应端口
            //!!这个端口的socket在监听入账连接
            this.serverChannel.socket().bind(new InetSocketAddress(this.settings.port()));
        }
    }
    
    void start() {
        this.logger.log(Level.FINEST, "Starting TCP node");
        this.please_stop = false;
        //把SelectThread赋给了node_thread
        (this.node_thread = new SelectThread()).setDaemon(true); //daemon thread：守护线程
        this.node_thread.start(); //开始select thread
        this.logger.log(Level.FINEST, "Started TCP node");
    }
    
    void wakeup() {
        this.logger.log(Level.FINEST, "Waking up selector thread");
        this.selector.wakeup();
    }
    
    void initiateStop(final long shutdown_deadline) {
        this.logger.log(Level.FINEST, "Initiating stop of TCP node");
        this.please_stop = true;
        this.shutdown_deadline = shutdown_deadline;
        this.logger.log(Level.FINEST, "Initiated stop of TCP node");
    }
    
    void join() {
        this.logger.log(Level.FINEST, "Joining selector thread");
        try {
            this.node_thread.join();
        }
        catch (InterruptedException ex) {}
        this.node_thread = null;
        this.logger.log(Level.FINEST, "Selector thread joined");
    }
    
    void closeIO() {
        this.logger.log(Level.FINEST, "Closing server channel, etc.");
        if (this.serverChannel != null) {
            try {
                this.serverChannel.close();
            }
            catch (IOException ex) {}
        }
        this.serverChannel = null;
        try {
            this.selector.close();
        }
        catch (IOException ex2) {}
        this.selector = null;
        this.logger.log(Level.FINEST, "Closed selector, etc.");
    }
    
    private void handleReadable(final TCPConnection tcpConnection) {
        this.logger.log(Level.FINEST, "handlereadable()...");
        tcpConnection.makeSpaceInNetInBuffer();
        final ConnectionBuffers connection_buffers = tcpConnection.connection_buffers;
        this.logger.log(Level.FINEST, "pre: conn.in_buffer.position=" + connection_buffers.netInBuffer().position());
        int read;
        try {
            int n = 0;
            while ((read = tcpConnection.channel.read(connection_buffers.netInBuffer())) > 0 && n++ < 3) {
                this.logger.log(Level.FINEST, "readloop: connection_buffers.netInBuffer().position=" + connection_buffers.netInBuffer().position());
                tcpConnection.makeSpaceInNetInBuffer();
            }
        }
        catch (IOException ex) {
            this.logger.log(Level.FINE, "got IOException", ex);
            this.closeConnection(tcpConnection);
            return;
        }
        tcpConnection.processNetInBuffer();
        this.processInBuffer(tcpConnection);
        if (read < 0 && tcpConnection.state != Connection.State.closed) {
            this.logger.log(Level.FINE, "count<0");
            this.closeConnection(tcpConnection);
        }
    }
    
    @SuppressWarnings("incomplete-switch")
	private void processInBuffer(final TCPConnection tcpConnection) {
        final ByteBuffer appInBuffer = tcpConnection.connection_buffers.appInBuffer();
        this.logger.log(Level.FINEST, "pre: app_in_buffer.position=" + appInBuffer.position());
        final int position = appInBuffer.position();
        final byte[] array = new byte[position];
        appInBuffer.position(0);
        appInBuffer.get(array);
        appInBuffer.position(position);
        int i = 0;
        while (i < array.length) {
            final int n = array.length - i;
            if (n < 4) {
                break;
            }
            final int decodeSize = Message.decodeSize(array, i);
            if (n < decodeSize) {
                break;
            }
            final Message message = new Message();
            final Message.decode_status decode = message.decode(array, i, decodeSize);
            switch (decode) {
                case decoded: {
                    this.logRawDecodedPacket(array, i, decodeSize);
                    i += decodeSize;
                    if (!this.handleMessage(message, tcpConnection)) {
                        this.logger.log(Level.FINER, "handle error");
                        this.closeConnection(tcpConnection);
                        return;
                    }
                    break;
                }
                case garbage: {
                    this.logGarbagePacket(tcpConnection, array, i, decodeSize);
                    this.closeConnection(tcpConnection, true);
                    return;
                }
            }
            if (decode == Message.decode_status.not_enough) {
                break;
            }
        }
        tcpConnection.consumeAppInBuffer(i);
    }
    
    private void handleWritable(final Connection connection) {
        final TCPConnection tcpConnection = (TCPConnection)connection;
        this.logger.log(Level.FINEST, "handleWritable():");
        final ByteBuffer netOutBuffer = tcpConnection.connection_buffers.netOutBuffer();
        netOutBuffer.flip();
        try {
            if (tcpConnection.channel.write(netOutBuffer) < 0) {
                this.closeConnection(tcpConnection);
                return;
            }
            netOutBuffer.compact();
            tcpConnection.processAppOutBuffer();
            if (!tcpConnection.hasNetOutput()) {
                tcpConnection.channel.register(this.selector, 1, tcpConnection);
            }
        }
        catch (IOException ex) {
            this.closeConnection(tcpConnection);
        }
    }
    
    void sendMessage(final TCPConnection tcpConnection, final byte[] array) {
        final boolean b = !tcpConnection.hasNetOutput();
        tcpConnection.makeSpaceInAppOutBuffer(array.length);
        tcpConnection.connection_buffers.appOutBuffer().put(array);
        tcpConnection.connection_buffers.processAppOutBuffer();
        if (b) {
            this.outputBecameAvailable(tcpConnection);
        }
    }
    
    private void outputBecameAvailable(final Connection connection) {
        final TCPConnection tcpConnection = (TCPConnection)connection;
        this.handleWritable(tcpConnection);
        if (tcpConnection.hasNetOutput()) {
            try {
                tcpConnection.channel.register(this.selector, 5, tcpConnection);
            }
            catch (ClosedChannelException ex) {}
        }
    }
    
    boolean initiateConnection(final Connection connection, final Peer peer) {
        final TCPConnection tcpConnection = (TCPConnection)connection;
        try {
            final SocketChannel open = SocketChannel.open();
            open.configureBlocking(false);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(peer.host(), peer.port());
            try {
                open.connect(inetSocketAddress);
            }
            catch (UnresolvedAddressException ex2) {
                return false;
            }
            tcpConnection.state = Connection.State.connecting;
            tcpConnection.channel = open;
            this.selector.wakeup();
            open.register(this.selector, 8, tcpConnection);
        }
        catch (IOException ex) {
            this.logger.log(Level.WARNING, "java.io.IOException caught while initiating connection to '" + peer.host() + "'.", ex);
        }
        return true;
    }
    
    void close(final Connection connection, final boolean b) {
        final TCPConnection tcpConnection = (TCPConnection)connection;
        try {
            tcpConnection.channel.register(this.selector, 0);
            if (b) {
                tcpConnection.channel.socket().setSoLinger(true, 0);
            }
            tcpConnection.channel.close();
        }
        catch (IOException ex) {}
    }
    
    Connection newConnection(final long n, final long n2) {
        return new TCPConnection(this, n, n2);
    }
    
    private class SelectThread extends Thread
    {
        public SelectThread() {
            super("DiameterNode thread (TCP)");
        }
        
        public void run() {
            try {
                this.run_();
                if (TCPNode.this.serverChannel != null) {
                    TCPNode.this.serverChannel.close();
                }
            }
            catch (IOException ex) {}
        }
        
        private void run_() throws IOException {
            if (TCPNode.this.serverChannel != null) {
                TCPNode.this.serverChannel.configureBlocking(false);
                //OP_ACCEPT = 1 << 4; 即16
                TCPNode.this.serverChannel.register(TCPNode.this.selector, 16);
            }
            while (true) {
                if (TCPNode.this.please_stop) {
                    if (System.currentTimeMillis() >= TCPNode.this.shutdown_deadline) {
                        break;
                    }
                    if (!TCPNode.this.anyOpenConnections()) {
                        break;
                    }
                }
                final long calcNextTimeout = TCPNode.this.calcNextTimeout();
                if (calcNextTimeout != -1L) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    if (calcNextTimeout > currentTimeMillis) {
                        TCPNode.this.selector.select(calcNextTimeout - currentTimeMillis);
                    }
                    else {
                        TCPNode.this.selector.selectNow();
                    }
                }
                else {
                    TCPNode.this.selector.select();
                }
//                if (TCPNode.this.selector.selectedKeys().size() > 1) {
//                	System.out.println("selected size > 1:" + TCPNode.this.selector.selectedKeys().size());
//                }

                final Iterator<SelectionKey> iterator = TCPNode.this.selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey selectionKey = iterator.next();
                    //Tests whether this key's channel is ready to accept a new socket connection.
                    if (selectionKey.isAcceptable()) {
//                    	System.out.println("进入selectionKey.isAcceptable()");
                        TCPNode.this.logger.log(Level.FINE, "Got an inbound connection (key is acceptable)");
                        final SocketChannel accept = ((ServerSocketChannel)selectionKey.channel()).accept();
                        final InetSocketAddress inetSocketAddress = (InetSocketAddress)accept.socket().getRemoteSocketAddress();
                        TCPNode.this.logger.log(Level.INFO, "Got an inbound connection from " + inetSocketAddress.toString());
                        if (!TCPNode.this.please_stop) {
                            final TCPConnection tcpConnection = new TCPConnection(TCPNode.this, TCPNode.this.settings.watchdogInterval(), TCPNode.this.settings.idleTimeout());
                            tcpConnection.host_id = inetSocketAddress.getAddress().getHostAddress();
                            tcpConnection.state = Connection.State.connected_in;
                            (tcpConnection.channel = accept).configureBlocking(false);
                            //OP_READ = 1 << 0; OP_WRITE = 1 << 2; OP_CONNECT = 1 << 3;
                            //tcpConnection为“附加组件”
                            accept.register(TCPNode.this.selector, 1, tcpConnection);  //只是Readable
                            
                            //registerInboundConnection这个函数的实现this.map_key_conn.put(connection.key, connection);
                            TCPNode.this.registerInboundConnection(tcpConnection); 
                        }
                        else {
                            accept.close();
                        }
                    }
                    else if (selectionKey.isConnectable()) {
//                    	System.out.println("进入selectionKey.isConnectable()");
                        TCPNode.this.logger.log(Level.FINE, "An outbound connection is ready (key is connectable)");
                        final SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                        final TCPConnection tcpConnection2 = (TCPConnection)selectionKey.attachment();
                        try {
                            if (socketChannel.finishConnect()) {
                                TCPNode.this.logger.log(Level.FINEST, "Connected!");	
                                tcpConnection2.state = Connection.State.connected_out;
                                socketChannel.register(TCPNode.this.selector, 1, tcpConnection2);
                                TCPNode.this.initiateCER(tcpConnection2);
                            }
                        }
                        catch (IOException ex) {
                            TCPNode.this.logger.log(Level.WARNING, "Connection to '" + tcpConnection2.host_id + "' failed", ex);
                            try {
                                socketChannel.register(TCPNode.this.selector, 0);
                                socketChannel.close();
                            }
                            catch (IOException ex2) {}
                            TCPNode.this.unregisterConnection(tcpConnection2);
                        }
                    }
                    else if (selectionKey.isReadable()) {
//                    	System.out.println("进入selectionKey.isReadable()");
                        TCPNode.this.logger.log(Level.FINEST, "Key is readable");
                        final SocketChannel socketChannel2 = (SocketChannel)selectionKey.channel();
                        final TCPConnection tcpConnection3 = (TCPConnection)selectionKey.attachment(); //得到“附加组件”
                        TCPNode.this.handleReadable(tcpConnection3); //处理缓冲区
                        if (tcpConnection3.state != Connection.State.closed && tcpConnection3.hasNetOutput()) {
                            socketChannel2.register(TCPNode.this.selector, 5, tcpConnection3); //注意：这里改变了这个channel需要被监听的事件：1+4(read和write)
                        }
                    }
                    else if (selectionKey.isWritable()) {
//                    	System.out.println("进入selectionKey.isWritable()");
                        TCPNode.this.logger.log(Level.FINEST, "Key is writable");
                        final SocketChannel socketChannel3 = (SocketChannel)selectionKey.channel();
                        final TCPConnection tcpConnection4 = (TCPConnection)selectionKey.attachment();
                        synchronized (TCPNode.this.getLockObject()) {
                            TCPNode.this.handleWritable(tcpConnection4);
                            if (tcpConnection4.state != Connection.State.closed && tcpConnection4.hasNetOutput()) {
                                socketChannel3.register(TCPNode.this.selector, 5, tcpConnection4);
                            }
                        }
                    }
                    iterator.remove();
                }
                TCPNode.this.runTimers();
            }
        }
    }
}
