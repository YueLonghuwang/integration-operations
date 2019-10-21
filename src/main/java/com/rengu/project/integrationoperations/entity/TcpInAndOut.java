package com.rengu.project.integrationoperations.entity;

import java.nio.ByteBuffer;

/**
 * @author yyc 存放接收的tcp数据
 *
 */
public class TcpInAndOut {
	private String host; // 连接的设备
	private ByteBuffer byteBuffer;

	public TcpInAndOut(String host, ByteBuffer byteBuffer) {
		super();
		this.host = host;
		this.byteBuffer = byteBuffer;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}
}
