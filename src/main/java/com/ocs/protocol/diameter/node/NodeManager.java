package com.ocs.protocol.diameter.node;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.ocs.protocol.diameter.*;

public class NodeManager implements MessageDispatcher, ConnectionListener {
	private Node node;
	private NodeSettings settings;
	private Map<ConnectionKey, Map<Integer, Object>> req_map;
	private Logger logger;

	public NodeManager(final NodeSettings nodeSettings) {
		this(nodeSettings, null);
	}

	public NodeManager(final NodeSettings settings,
			final NodeValidator nodeValidator) {
		super();
		this.node = new Node(this, this, settings, nodeValidator);
		this.settings = settings;
		this.req_map = new HashMap<ConnectionKey, Map<Integer, Object>>();
		this.logger = Logger.getLogger("com.ocs.protocol.diameter.node");
	}

	public void start() throws IOException,
			UnsupportedTransportProtocolException {
		this.node.start();
	}

	public void stop() {
		this.stop(0L);
	}

	public void stop(final long n) {
		this.node.stop(n);
		synchronized (this.req_map) {
			for (final Map.Entry<ConnectionKey, Map<Integer, Object>> entry : this.req_map
					.entrySet()) {
				final ConnectionKey connectionKey = entry.getKey();
				final Iterator<Map.Entry<Integer, Object>> iterator2 = entry
						.getValue().entrySet().iterator();
				while (iterator2.hasNext()) {
					this.handleAnswer(null, connectionKey, iterator2.next()
							.getValue());
				}
			}
		}
		this.req_map = new HashMap<ConnectionKey, Map<Integer, Object>>();
	}

	public void waitForConnection() throws InterruptedException {
		this.node.waitForConnection();
	}

	public void waitForConnection(final long n) throws InterruptedException {
		this.node.waitForConnection(n);
	}

	public Node node() {
		return this.node;
	}

	public NodeSettings settings() {
		return this.settings;
	}

	protected void handleRequest(final Message message,
			final ConnectionKey connectionKey, final Peer peer) {
		final Message mandatory_RFC3588 = new Message();
		this.logger.log(Level.FINE, "Handling incoming request, command_code="
				+ message.hdr.command_code + ", peer=" + peer.host()
				+ ", end2end=" + message.hdr.end_to_end_identifier
				+ ", hopbyhop=" + message.hdr.hop_by_hop_identifier);
		mandatory_RFC3588.prepareResponse(message);
		mandatory_RFC3588.hdr.setError(true);
		mandatory_RFC3588.add(new AVP_Unsigned32(268, 3002));
		this.node.addOurHostAndRealm(mandatory_RFC3588);
		Utils.copyProxyInfo(message, mandatory_RFC3588);
		Utils.setMandatory_RFC3588(mandatory_RFC3588);
		try {
			this.answer(mandatory_RFC3588, connectionKey);
		} catch (NotAnAnswerException ex) {
		}
	}

	protected void handleAnswer(final Message message,
			final ConnectionKey connectionKey, final Object o) {
		this.logger.log(Level.FINE, "Handling incoming answer, command_code="
				+ message.hdr.command_code + ", end2end="
				+ message.hdr.end_to_end_identifier + ", hopbyhop="
				+ message.hdr.hop_by_hop_identifier);
	}

	public final void answer(final Message message,
			final ConnectionKey connectionKey) throws NotAnAnswerException {
		if (message.hdr.isRequest()) {
			throw new NotAnAnswerException();
		}
		try {
			this.node.sendMessage(message, connectionKey);
		} catch (StaleConnectionException ex) {
		}
	}

	protected final void forwardRequest(final Message message,
			final ConnectionKey connectionKey, final Object o)
			throws StaleConnectionException, NotARequestException,
			NotProxiableException {
		if (!message.hdr.isProxiable()) {
			throw new NotProxiableException();
		}
		boolean b = false;
		final String hostId = this.settings.hostId();
		final Iterator<AVP> iterator = message.subset(282).iterator();
		while (iterator.hasNext()) {
			if (new AVP_UTF8String(iterator.next()).queryValue().equals(hostId)) {
				b = true;
				break;
			}
		}
		if (!b) {
			message.add(new AVP_UTF8String(282, this.settings.hostId()));
		}
		this.sendRequest(message, connectionKey, o);
	}

	protected final void forwardAnswer(final Message message,
			final ConnectionKey connectionKey) throws StaleConnectionException,
			NotAnAnswerException, NotProxiableException {
		if (!message.hdr.isProxiable()) {
			throw new NotProxiableException();
		}
		if (message.hdr.isRequest()) {
			throw new NotAnAnswerException();
		}
		message.add(new AVP_UTF8String(282, this.settings.hostId()));
		this.answer(message, connectionKey);
	}

	public final void sendRequest(final Message message,
			final ConnectionKey connectionKey, final Object o)
			throws StaleConnectionException, NotARequestException {
		if (!message.hdr.isRequest()) {
			throw new NotARequestException();
		}
		message.hdr.hop_by_hop_identifier = this.node
				.nextHopByHopIdentifier(connectionKey);
		synchronized (this.req_map) {
			final Map<Integer, Object> map = this.req_map.get(connectionKey);
			if (map == null) {
				throw new StaleConnectionException();
			}
			map.put(message.hdr.hop_by_hop_identifier, o);
		}
		try {
			this.node.sendMessage(message, connectionKey);
			this.logger.log(Level.FINER, "Request sent, command_code="
					+ message.hdr.command_code + " hop_by_hop_identifier="
					+ message.hdr.hop_by_hop_identifier);
		} catch (StaleConnectionException ex) {
			synchronized (this.req_map) {
				this.req_map.remove(message.hdr.hop_by_hop_identifier);
			}
			throw ex;
		}
	}

	public final void sendRequest(final Message message, final Peer[] array,
			final Object o) throws NotRoutableException, NotARequestException {
		this.logger.log(Level.FINER, "Sending request (command_code="
				+ message.hdr.command_code + ") to " + array.length + " peers");
		message.hdr.end_to_end_identifier = this.node.nextEndToEndIdentifier();
		boolean b = false;
		boolean b2 = false;
		for (final Peer peer : array) {
			b = true;
			this.logger.log(Level.FINER, "Considering sending request to "
					+ peer.host());
			final ConnectionKey connection = this.node.findConnection(peer);
			if (connection != null) {
				final Peer connectionKey2Peer = this.node
						.connectionKey2Peer(connection);
				if (connectionKey2Peer != null) {
					if (!this.node.isAllowedApplication(message,
							connectionKey2Peer)) {
						this.logger.log(Level.FINER, "peer " + peer.host()
								+ " cannot handle request");
					} else {
						b2 = true;
						try {
							this.sendRequest(message, connection, o);
							return;
						} catch (StaleConnectionException ex) {
							this.logger.log(Level.FINE,
									"Setting retransmit bit");
							message.hdr.setRetransmit(true);
						}
					}
				}
			}
		}
		if (b2) {
			throw new NotRoutableException(
					"All capable peer connections went stale");
		}
		if (b) {
			throw new NotRoutableException("No capable peers");
		}
		throw new NotRoutableException();
	}

	//实现MessageDispatcher接口
	public final boolean handle(final Message message,
			final ConnectionKey connectionKey, final Peer peer) {
//		System.out.println("有执行吗？22");
		if (message.hdr.isRequest()) {
			this.logger.log(Level.FINER, "Handling request");
			this.handleRequest(message, connectionKey, peer);
		} else {
			this.logger.log(Level.FINER,
					"Handling answer, hop_by_hop_identifier="
							+ message.hdr.hop_by_hop_identifier);
			Object value = null;
			synchronized (this.req_map) {
				final Map<Integer, Object> map = this.req_map
						.get(connectionKey);
				if (map != null) {
					value = map.get(message.hdr.hop_by_hop_identifier);
					map.remove(message.hdr.hop_by_hop_identifier);
				}
			}
			if (value != null) {
				this.handleAnswer(message, connectionKey, value);
			} else {
				this.logger.log(Level.FINER,
						"Answer did not match any outstanding request");
			}
		}
		return true;
	}

	//实现ConnectionListener接口
	//    connkey - The connection key.
	//    peer - The peer the connection is to.
	//    b - True if the connection has been established. False if the connection has been lost.
	public final void handle(final ConnectionKey connectionKey,
			final Peer peer, final boolean b) {
		synchronized (this.req_map) {
			if (b) {
				this.req_map.put(connectionKey, new HashMap<Integer, Object>());
			} else {
				final Map<Integer, Object> map = this.req_map
						.get(connectionKey);
				if (map == null) {
					return;
				}
				this.req_map.remove(connectionKey);
				final Iterator<Map.Entry<Integer, Object>> iterator = map
						.entrySet().iterator();
				while (iterator.hasNext()) {
					this.handleAnswer(null, connectionKey, iterator.next()
							.getValue());
				}
			}
		}
	}
}
