package com.rengu.project.integrationoperations.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rengu.project.integrationoperations.entity.ReparedHeartBeat;
import com.rengu.project.integrationoperations.entity.TcpInAndOut;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import com.rengu.project.integrationoperations.thread.TCPThread;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServerTimer {

	@Autowired
	private WebReceiveToCService WebReceiveToCService;

	@Scheduled(fixedRate = 3)
	public void com() throws InterruptedException {
		if (!TCPThread.queue.isEmpty()) {
			TcpInAndOut tcpInAndOut = TCPThread.queue.take();
			WebReceiveToCService.receiveSocketHandler1(tcpInAndOut.getByteBuffer(), tcpInAndOut.getHost());
		}
	}
}
