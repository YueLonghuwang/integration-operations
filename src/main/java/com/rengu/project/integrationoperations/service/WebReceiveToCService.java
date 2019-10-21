package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.repository.SysErrorLogRepository;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 接收c++发送数据，进行解析 author : yaojiahao Date: 2019/7/8 11:19
 **/

@Service
@Slf4j
public class WebReceiveToCService {
	private final HostRepository hostRepository;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private static DecimalFormat df2 = new DecimalFormat("#.00");
	private static DecimalFormat jd = new DecimalFormat("#.0000");
	private static String radarString = "";
	private final SysErrorLogService sysErrorLogService;
	Map<String, String> globalMap = new HashMap<String, String>();
	Map<String, String> globalId = new HashMap<String, String>(); // 全局站id

	public WebReceiveToCService(HostRepository hostRepository, SimpMessagingTemplate simpMessagingTemplate,
								SysErrorLogService sysErrorLogService) {
		this.hostRepository = hostRepository;
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.sysErrorLogService = sysErrorLogService;
	}

	// 储存或更新当前连接服务端的IP地址
	public void allHost(String hosts) {
		List<AllHost> allHostList = hostRepository.findAll();
		int size = hostRepository.findByHostNotLike("无").size();
		for (AllHost allHost : allHostList) {
			if (allHost.getHost().equals("无")) {
				if (!hasHostIP(hosts)) {
					allHost.setHost(hosts);
					//获取站id保存
//					if (globalId.get("hostID").equals(null) && globalId.get("hostID") == "") {
//						allHost.setStationID("0");
//					} else {
//						allHost.setStationID(String.valueOf(globalId.get("hostID")));
//					}
					allHost.setStationID(String.valueOf(globalId.get("hostID")));
					Map<Object, Object> map = new HashMap<>();
					map.put("device", size + 1);
					map.put("message", "一台新的设备已入库");
					hostRepository.save(allHost);
					simpMessagingTemplate.convertAndSend("/deviceConnect/send", map);
					return;
				}
			}
		}
		if (!hasHostIP(hosts)) {
			AllHost allHosts = new AllHost();
			allHosts.setHost(hosts);
			allHosts.setStationID(String.valueOf(globalId.get("hostID")));
			AllHost allHost = hostRepository.findMaxByNum();
			allHosts.setNum(allHost.getNum() + 1);
			hostRepository.save(allHosts);
		}
	}

	// 解析报文固定信息
	@Async
	public Map<String, Number> receiveFixedInformation(ByteBuffer byteBuffer) {
		Map<String, Number> map = new HashMap<>();
		map.put("header", byteBuffer.getInt(0)); // 报文头
		map.put("dataLength", byteBuffer.getInt(4)); // 当前包数据长度
		map.put("targetHost", byteBuffer.getShort(8)); // 目的地址
		map.put("sourceHost", byteBuffer.getShort(10)); // 源地址
		map.put("regionID", byteBuffer.get(12)); // 域ID
		map.put("themeID", byteBuffer.get(13)); // 主题ID
		map.put("messageCategory", byteBuffer.getShort(14)); // 信息类别
		map.put("transmitDate", byteBuffer.getLong(16)); // 发报日期时间
		map.put("serialNumber", byteBuffer.getInt(24)); // 序列号
		map.put("bagTotal", byteBuffer.getInt(28)); // 包总数
		map.put("currentBagNo", byteBuffer.getInt(32)); // 当前包号
		map.put("dataTotalLength", byteBuffer.getInt(36)); // 数据总长度
		map.put("versionNumber", byteBuffer.getShort(40)); // 版本号
		map.put("backups1", byteBuffer.getInt(42)); // 保留字段
		map.put("backups2", byteBuffer.getShort(46)); // 保留字段
		return map;
	}

	/**
	 * 设备
	 */
//	@Async
	public void receiveSocketHandler1(ByteBuffer byteBuffer, String host) {
		int messageCategorys = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort(14);
		System.out.println("接收到" + messageCategorys);
//		Map<String, Number> mapFixation = receiveFixedInformation(byteBuffer);
//		String sourceHost = Integer.toHexString(Short.toUnsignedInt(mapFixation.get("sourceHost").shortValue()));
//		globalId.put("hostID", sourceHost);
		switch (messageCategorys) {
			case 12289:
				System.out.println("接收到" + host + "的心跳指令");
				receiveHeartbeatCMD(byteBuffer, host); // 心跳指令
				break;
			case 12545:
				System.out.println("接收到" + host + "的上报心跳信息");
				uploadHeartBeatMessage(byteBuffer, host);// 上报心跳信息
				break;
			case 12546:
				System.out.println("接收到" + host + "的上报自检结果");
				uploadSelfInspectionResult(byteBuffer, host); // 上报自检结果
				break;
			case 12549:
				System.out.println("接收到" + host + "的软件版本远程更新");
				uploadSoftwareVersionMessage(byteBuffer, host);// 软件版本远程更新
				break;
			case 12550:
				System.out.println("接收到" + host + "的设备网络参数更新");
				uploadDeviceNetWorkParamMessage(byteBuffer, host);// 设备网络参数更新
				break;
			case 12576:
				System.out.println("接收到" + host + "的上报雷达子系统工作状态");
				uploadRadarSubSystemWorkStatusMessage(byteBuffer, host);// 上报雷达子系统工作状态
				break;
			case 12577:
				System.out.println("接收到" + host + "的敌我子系统工作状态");
				uploadEnemyAndUsPackageMessage(byteBuffer, host);// 敌我子系统工作状态
				break;
			default:
//            test(byteBuffer, host);
				break;
		}
	}

	/**
	 * 3.4.6.2 心跳指令
	 */
	private void receiveHeartbeatCMD(ByteBuffer byteBuffer, String host) {

		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		Map<String, Object> map1 = new HashMap<>();
//        map.put("messageLength",byteBuffer.getInt(48));// 信息长度
//        map1.put("taskFlowNo", byteBuffer.getLong(52));// 任务流水号
		map1.put("heartbeat", byteBuffer.get(60));// 心跳
//        map.put("verify", byteBuffer.getInt(61));// 校验和
//        map.put("messageEnd", byteBuffer.getInt(65));// 结尾
		map1.put("host", host);
		ResultEntity resultEntity = new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1);
		simpMessagingTemplate.convertAndSend("/receiveHeartbeatCMD/sendToHeartBeat", resultEntity);
	}

	/**
	 * 3.4.6.9 上传心跳信息
	 */
	private void uploadHeartBeatMessage(ByteBuffer byteBuffer, String host) {
		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Map<String, Number> mapFixation = receiveFixedInformation(byteBuffer);
		Map<String, String> result = new HashMap<String, String>(); // 解析系统工作状态
		byte systemWorkStatus = byteBuffer.get(60);
		String cardState = getCardState(systemWorkStatus);
		String sourceHost = Integer.toHexString(Short.toUnsignedInt(mapFixation.get("sourceHost").shortValue()));
		result.put("cardState", cardState);
		result.put("host", host);
		result.put("stationId", sourceHost);
		globalId.put("hostID", sourceHost);
		simpMessagingTemplate.convertAndSend("/uploadHeartBeatMessage/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, result));
		saveErrorLog(host, cardState);
	}

	/**
	 * 3.4.6.10 上报自检结果
	 */
	private void uploadSelfInspectionResult(ByteBuffer byteBuffer, String host) {
		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		Map<String, String> map1 = new HashMap<String, String>();
		int messageLength = byteBuffer.getInt(48); // 信息长度
		long taskFlowNo = byteBuffer.getLong(52); // 任务流水号
		map1.put("taskFlowNo", String.valueOf(taskFlowNo));
		byte systemWorkStatus = byteBuffer.get(60); // 自检结果
		// getDouble(systemWorkStatus);
		String systemWork = Integer.toBinaryString(systemWorkStatus);
		StringBuilder stringBuilder2 = new StringBuilder(systemWork);
		stringBuilder2.reverse();
		StringBuilder stringBuilder4 = new StringBuilder();
		int a1 = 8 - systemWork.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			// stringBuilder3.append(b);
			systemWork = bString1 + systemWork;
		}
		// 每两位转10进制数值int输出
		for (int i = 4; i < 8; i += 2) {
			String rstr = systemWork.substring(i, i + 2);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			stringBuilder4.append(ss);
		}
		map1.put("systemWorkStatus", String.valueOf(stringBuilder4.reverse()));
		// map1.put("systemWorkStatus", String.valueOf(systemWorkStatus));
		byteBuffer.position(61);
		// 备份
		byte[] selfChoose = new byte[3];
		byteBuffer.get(selfChoose);
		BigDecimal bigDecimal = null;
		// 雷达子系统自检信息
		// 通道1频率1
		int radarChannel1Frequency = byteBuffer.getInt(64);
		bigDecimal = new BigDecimal((float) radarChannel1Frequency * 0.1);
		map1.put("radarChannel1Frequency", bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 脉宽1
		int radarChannel1PulseWidth = byteBuffer.getInt(68);
		bigDecimal = new BigDecimal((float) radarChannel1PulseWidth * 0.008);
		map1.put("radarChannel1PulseWidth", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 重周1
		int radarChannel1RepeatPeriod = byteBuffer.getInt(72);
		bigDecimal = new BigDecimal((float) radarChannel1RepeatPeriod * 0.008);
		map1.put("radarChannel1RepeatPeriod", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 数字幅度1
		byte radarChannel1NumRange = byteBuffer.get(76);
		bigDecimal = new BigDecimal((float) Byte.toUnsignedInt(radarChannel1NumRange) * 0.25);
		map1.put("radarChannel1NumRange", bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 通道2频率
		int radarChannel2Frequency = byteBuffer.getInt(77);
		bigDecimal = new BigDecimal((float) radarChannel2Frequency * 0.1);
		map1.put("radarChannel2Frequency", bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 脉宽
		int radarChannel2PulseWidth = byteBuffer.getInt(81);
		bigDecimal = new BigDecimal((float) radarChannel2PulseWidth * 0.008);
		map1.put("radarChannel2PulseWidth", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 重周
		int radarChannel2RepeatPeriod = byteBuffer.getInt(85);
		bigDecimal = new BigDecimal((float) radarChannel2RepeatPeriod * 0.008);
		map1.put("radarChannel2RepeatPeriod", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 通道2数字幅度
		byte radarChannel2NumRange = byteBuffer.get(89);
		bigDecimal = new BigDecimal((float) Byte.toUnsignedInt(radarChannel2NumRange) * 0.25);
		map1.put("radarChannel2NumRange", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		/** 敌我子系统自检信息 */
		// 频率
		int enemyAndUsFrequency = byteBuffer.getInt(90);
		bigDecimal = new BigDecimal((float) enemyAndUsFrequency * 0.0625);
		map1.put("enemyAndUsFrequency", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 脉宽
		int enemyAndUsPulseWidth = byteBuffer.getInt(94);
		bigDecimal = new BigDecimal((float) enemyAndUsPulseWidth * 0.00416667);
		map1.put("enemyAndUsPulseWidth", bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
		// 重周
		int enemyAndUsRepeatPeriod = byteBuffer.getInt(98);
		map1.put("enemyAndUsRepeatPeriod", String.valueOf(enemyAndUsRepeatPeriod));
		// 数字幅度
		byte enemyAndUsNumRange = byteBuffer.get(102);
		map1.put("enemyAndUsNumRange", String.valueOf(enemyAndUsNumRange));
		int verify = byteBuffer.getInt(103); // 校验和
		int messageEnd = byteBuffer.getInt(107); // 报文尾
		map1.put("host", host);
		simpMessagingTemplate.convertAndSend("/uploadSelfInspectionResult/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
	}

	/**
	 * 3.4.6.11 上报软件版本信息包 (软件版本信息表128字节待定,已用72字节)
	 */
	private void uploadSoftwareVersionMessage(ByteBuffer byteBuffer, String host) {
		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		Map<String, String> map1 = new HashMap<>();
		int messageLength = byteBuffer.getInt(48);
		long taskFlowNo = byteBuffer.getLong(52);
		short cmd = byteBuffer.getShort(60);
		// 软件版本信息表 ()
		// 雷达加载版本号(先展示前72个字节)
		int lateralFPGA = byteBuffer.getInt(62); // 侧向FPGA_A版本号
		String lateral = Integer.toHexString(lateralFPGA).toUpperCase();
		map1.put("lateralFPGA", String.valueOf(lateral));
		int lateralZ7PS = byteBuffer.getInt(66); // 侧向Z7-PS版本号
		String lateral2 = Integer.toHexString(lateralZ7PS).toUpperCase();
		map1.put("lateralZ7PS", lateral2);
		int crimesFPGA = byteBuffer.getInt(70);
		String crimes = Integer.toHexString(crimesFPGA).toUpperCase();
		map1.put("crimesFPGA", crimes);
		int crimesZ7PS = byteBuffer.getInt(74);
		String crimes2 = Integer.toHexString(crimesZ7PS).toUpperCase();
		map1.put("crimesZ7PS", crimes2);
		int signalFPGA = byteBuffer.getInt(78);
		String signal = Integer.toHexString(signalFPGA).toUpperCase();
		map1.put("signalFPGA", signal);
		int systemFPGA = byteBuffer.getInt(82);
		String system = Integer.toHexString(systemFPGA).toUpperCase();
		map1.put("systemFPGA", system);
		int lateralFPGB = byteBuffer.getInt(86);
		String lateral3 = Integer.toHexString(lateralFPGB).toUpperCase();
		map1.put("lateralFPGB", lateral3);
		int lateralZ7PL = byteBuffer.getInt(90);
		String lateral4 = Integer.toHexString(lateralZ7PL).toUpperCase();
		map1.put("lateralZ7PL", lateral4);
		int crimesFPGB = byteBuffer.getInt(94);
		String crimes3 = Integer.toHexString(crimesFPGB).toUpperCase();
		map1.put("crimesFPGB", crimes3);
		int crimesZ7PL = byteBuffer.getInt(98);
		String crimes4 = Integer.toHexString(crimesZ7PL).toUpperCase();
		map1.put("crimesZ7PL", crimes4);
		int signalDSP = byteBuffer.getInt(102);
		String signaldsp = Integer.toHexString(signalDSP).toUpperCase();
		map1.put("signalDSP", signaldsp);
		int systemDSP = byteBuffer.getInt(106);
		String systemdsp = Integer.toHexString(systemDSP).toUpperCase();
		map1.put("systemDSP", systemdsp);
		// 敌我加载版本号
		int masterControlFPGA1 = byteBuffer.getInt(110);
		String master = Integer.toHexString(masterControlFPGA1).toUpperCase();
		map1.put("masterControlFPGA1", master);
		int masterControlDSP = byteBuffer.getInt(114);
		String master1 = Integer.toHexString(masterControlDSP).toUpperCase();
		map1.put("masterControlDSP", master1);
		int inspectFPGA2 = byteBuffer.getInt(118);
		String inspect = Integer.toHexString(inspectFPGA2).toUpperCase();
		map1.put("inspectFPGA2", inspect);
		int masterControlFPGA2 = byteBuffer.getInt(122);
		String masterControl = Integer.toHexString(masterControlFPGA2).toUpperCase();
		map1.put("masterControlFPGA2", masterControl);
		int inspectFPGA1 = byteBuffer.getInt(126);
		String inspectfpga = Integer.toHexString(inspectFPGA1).toUpperCase();
		map1.put("inspectFPGA1", inspectfpga);
		int inspectFPGA = byteBuffer.getInt(130);
		String inspect2 = Integer.toHexString(inspectFPGA).toUpperCase();
		map1.put("inspectFPGA", inspect2);
		byteBuffer.position(134);
		// 信息表长度占256，版本信息占了72个字节
		byte[] bytes1 = new byte[184];
		byteBuffer.get(bytes1);
		int verify = byteBuffer.getInt(318); // 校验和
		int messageEnd = byteBuffer.getInt(322); // 报文尾
		map1.put("host", host);
		simpMessagingTemplate.convertAndSend("/uploadSoftwareVersionMessage/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
	}

	/**
	 * 3.4.6.12 上传设备网络参数信息包
	 */
	private void uploadDeviceNetWorkParamMessage(ByteBuffer byteBuffer, String host) {

		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		SendDeviceNetWorkParam sendDeviceNetWorkParam = new SendDeviceNetWorkParam();
		int messageLength = byteBuffer.getInt(48);
		long taskFlowNo = byteBuffer.getLong(52);
		short cmd = byteBuffer.getShort(60);
		short networkID = byteBuffer.getShort(62); // 网络终端ID号
		sendDeviceNetWorkParam.setNetworkID(String.valueOf(networkID));
		byteBuffer.position(64);
		byte[] networkIP1s = new byte[4];
		byteBuffer.get(networkIP1s);
		sendDeviceNetWorkParam.setNetworkIP1(getIP(networkIP1s)); // 网络IP地址1
		String mac1 = getMac(byteBuffer.getShort(68), byteBuffer.getInt(70)); // 网络MAC地址1
		sendDeviceNetWorkParam.setNetworkMacIP1(mac1);
		int networkMessage1 = byteBuffer.getInt(74); // 网络端口信息1
		sendDeviceNetWorkParam.setNetworkMessage1(String.valueOf(networkMessage1));
		byteBuffer.position(78);
		byte[] networkIP2s = new byte[4];
		byteBuffer.get(networkIP2s);
		sendDeviceNetWorkParam.setNetworkIP2(getIP(networkIP2s)); // 网络IP地址2
		String mac2 = getMac(byteBuffer.getShort(82), byteBuffer.getInt(86)); // 网络MAC地址2
		sendDeviceNetWorkParam.setNetworkMacIP2(mac2);
		int networkMessage2 = byteBuffer.getInt(88); // 网络端口信息2
		sendDeviceNetWorkParam.setNetworkMessage2(String.valueOf(networkMessage2));

		byteBuffer.position(92);
		byte[] networkIP3s = new byte[4];
		byteBuffer.get(networkIP3s);
		sendDeviceNetWorkParam.setNetworkIP3(getIP(networkIP3s)); // 网络IP地址3

		String mac3 = getMac(byteBuffer.getShort(96), byteBuffer.getInt(98)); // 网络MAC地址3
		sendDeviceNetWorkParam.setNetworkMacIP3(mac3);
		int networkMessage3 = byteBuffer.getInt(102); // 网络端口信息3
		sendDeviceNetWorkParam.setNetworkMessage3(String.valueOf(networkMessage3));

		byteBuffer.position(106);
		byte[] networkIP4s = new byte[4];
		byteBuffer.get(networkIP4s);
		sendDeviceNetWorkParam.setNetworkIP4(getIP(networkIP4s)); // 网络IP地址4

		String mac4 = getMac(byteBuffer.getShort(110), byteBuffer.getInt(112)); // 网络MAC地址4
		sendDeviceNetWorkParam.setNetworkMacIP4(mac4);
		int networkMessage4 = byteBuffer.getInt(116); // 网络端口信息4
		sendDeviceNetWorkParam.setNetworkMessage4(String.valueOf(networkMessage4));

		byteBuffer.position(120);
		byte[] networkIP5s = new byte[4];
		byteBuffer.get(networkIP5s);
		sendDeviceNetWorkParam.setNetworkIP5(getIP(networkIP5s)); // 网络IP地址5

		String mac5 = getMac(byteBuffer.getShort(124), byteBuffer.getInt(126)); // 网络MAC地址5
		sendDeviceNetWorkParam.setNetworkMacIP5(mac5);
		int networkMessage5 = byteBuffer.getInt(130); // 网络端口信息5
		sendDeviceNetWorkParam.setNetworkMessage5(String.valueOf(networkMessage5));

		byteBuffer.position(134);
		byte[] networkIP6s = new byte[4];
		byteBuffer.get(networkIP6s);
		sendDeviceNetWorkParam.setNetworkIP6(getIP(networkIP6s)); // 网络IP地址6
		String mac6 = getMac(byteBuffer.getShort(138), byteBuffer.getInt(140)); // 网络MAC地址6
		sendDeviceNetWorkParam.setNetworkMacIP6(mac6);
		int networkMessage6 = byteBuffer.getInt(144); // 网络端口信息6
		sendDeviceNetWorkParam.setNetworkMessage6(String.valueOf(networkMessage6));
		// 结尾
		int verify = byteBuffer.getInt(148); // 校验和
		int messageEnd = byteBuffer.getInt(152); // 报文尾
		List<Object> list = new ArrayList<>();
		list.add(host);
		list.add(sendDeviceNetWorkParam);
		// 根据设备发送指定信息
		simpMessagingTemplate.convertAndSend("/uploadDeviceNetWorkParamMessage/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, list));
	}

	/**
	 * 3.4.6.13 上报雷达子系统工作状态信息包 雷达子系统状态信息
	 */
	@Async
	public void uploadRadarSubSystemWorkStatusMessage(ByteBuffer byteBuffer, String host) {
		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		Map<String, String> map1 = new HashMap<>();
		int messageLength = byteBuffer.getInt(48);
		long taskFlowNo = byteBuffer.getLong(52);
		// 雷达子系统状态信息
		short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort(60)); // 帧有效标记
		short dataType = byteBuffer.getShort(62); // 数据类型
		int dataLength = byteBuffer.getInt(64); // 数据长度
		/* 系统控制信息begin */
		// 包头 53f8(2)
		short heardSys = byteBuffer.getShort(68);
		map1.put("heardSys", Integer.toHexString(Short.toUnsignedInt(heardSys)));
		// 雷达信息包序号 (2)
		short systemControl = byteBuffer.getShort(70);
		map1.put("systemControl", Short.toUnsignedInt(systemControl) + "");
		// TODO 时间码 8
		long timeCodings = byteBuffer.getLong(72);
		String timeCode = longToBinaryString(timeCodings);

		String resultString = "";
		// 年
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(0, 16), 2)) + "/";
		// 月
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(16, 24), 2)) + "/";
		// 日
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(24, 32), 2)) + " ";
		// 时
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(56, 64), 2)) + ":";
		// 分
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(48, 56), 2)) + ":";
		// 秒
		resultString = resultString + String.valueOf(Short.parseShort(timeCode.substring(32, 48), 2));
		map1.put("timeCodings", resultString);
		// 工作方式(1) <string>自检</string>
		byte workPattern = byteBuffer.get(80);
		map1.put("workPattern", Byte.toUnsignedInt(workPattern) + "");
		// 带宽选择(1)
		byte bandWidthSelection = byteBuffer.get(81);
		map1.put("bandWidthSelection", Byte.toUnsignedInt(bandWidthSelection) + "");
		// 工作周期数(1)
		byte workNums = byteBuffer.get(82);
		map1.put("workNums", Byte.toUnsignedInt(workNums) + "");
		// 工作周期长度( 1
		byte workPeriodNum = byteBuffer.get(83);
		map1.put("workPeriodNum", Byte.toUnsignedInt((byte) (workPeriodNum * 50)) + "");
		// 频段码--2
		short frequencyPeriodNum = byteBuffer.getShort(84);
		map1.put("frequencyPeriodNum", Short.toUnsignedInt(frequencyPeriodNum) + "");
		// 测向天线选择-- 1
		byte cxChooce = byteBuffer.get(86);
		map1.put("cxChooce", Byte.toUnsignedInt(cxChooce) + "");
		// 侦察天线选择--1
		byte zcChoose = byteBuffer.get(87);
		map1.put("zcChoose", Byte.toUnsignedInt(zcChoose) + "");
		// 射频1衰减--1
		byte sp1 = byteBuffer.get(88);
		map1.put("sp1", Byte.toUnsignedInt(sp1) + "");
		// 射频2衰减--1
		byte sp2 = byteBuffer.get(89);
		map1.put("sp2", Byte.toUnsignedInt(sp2) + "");
		// 中频2衰减--1
		byte mid1 = byteBuffer.get(90);
		map1.put("mid1", Byte.toUnsignedInt(mid1) + "");
		// 中频1衰减--1
		byte mid2 = byteBuffer.get(91);
		map1.put("mid2", Byte.toUnsignedInt(mid2) + "");
		byteBuffer.position(92);
		// 备份--2
		byte[] byteSys = new byte[2];
		byteBuffer.get(byteSys);
		// 脉冲筛选最小频率--2
		short filterMinimumFrequency = byteBuffer.getShort(94);
		map1.put("filterMinimumFrequency", Short.toUnsignedInt(filterMinimumFrequency) + "");
		// 脉冲筛选最大频率--2
		short filterMaximumFrequency = byteBuffer.getShort(96);
		map1.put("filterMaximumFrequency", Short.toUnsignedInt(filterMaximumFrequency) + "");
		// 脉冲筛选最小幅度--1
		int minamplitude = Byte.toUnsignedInt(byteBuffer.get(98));
		map1.put("minamplitude", Integer.toUnsignedString((int) (minamplitude * 0.5)));
		// 脉冲筛选最大幅度--1
		int maxamplitude = Byte.toUnsignedInt(byteBuffer.get(99));
		map1.put("maxamplitude", Integer.toUnsignedString((int) (maxamplitude * 0.5)));
		// 脉冲筛选最小脉宽/100ns --2
		short minPulsewidth = byteBuffer.getShort(100);
		map1.put("minPulsewidth", Short.toUnsignedInt(minPulsewidth) + "");
		// 脉冲筛选最大脉宽/100ns --2
		short maxPulsewidth = byteBuffer.getShort(102);
		map1.put("maxPulsewidth", Short.toUnsignedInt(maxPulsewidth) + "");
		byteBuffer.position(104);
		// 信道屏蔽</16
		byte[] information = new byte[16];
		byteBuffer.get(information);
		// 脉内引导批次号开关</1
		byte open = byteBuffer.get(120);
		map1.put("open", Byte.toUnsignedInt(open) + "");
		// 脉内引导批次号</1
		byte openNum = byteBuffer.get(121);
		map1.put("openNum", Byte.toUnsignedInt(openNum) + "");
		// 需要上传的全脉冲数/--2
		short upNum = byteBuffer.getShort(122);
		map1.put("upNum", Short.toUnsignedInt(upNum) + "");
		// 分机控制----2
		short defendController = byteBuffer.getShort(124);
		String defend = getFenjiControl(defendController);
		map1.put("defendController", defend);
		// 设备编号--1
		byte deviceNo2 = byteBuffer.get(126);
		map1.put("deviceNo2", Byte.toUnsignedInt(deviceNo2) + radarString);
		// 检测门限调节 1
		byte textDoor = byteBuffer.get(127);
		map1.put("textDoor", Byte.toUnsignedInt(textDoor) + radarString);
		byteBuffer.position(128);
		// 备份 4
		byte[] backupSys = new byte[4];
		byteBuffer.get(backupSys);
		// byte[] bytes2 = new byte[64]; // GPS数据
		// byteBuffer.get(bytes2);

		/* GPS数据begin */
		// GPS包头 4
		int GPSHeader = byteBuffer.getInt(132);
		map1.put("GPSHeader", Integer.toHexString(GPSHeader));
		// 时
		byte hour = byteBuffer.get(136);
		map1.put("hour", Byte.toUnsignedInt(hour) + "");
		// 分
		byte mini = byteBuffer.get(137);
		map1.put("mini", Byte.toUnsignedInt(mini) + "");
		// 秒
		short second = byteBuffer.getShort(138);
		map1.put("second", Short.toUnsignedInt(second) + "");
		// 日
		byte days = byteBuffer.get(140);
		map1.put("days", Byte.toUnsignedInt(days) + "");
		// 月
		byte mouths = byteBuffer.get(141);
		map1.put("mouths", Byte.toUnsignedInt(mouths) + "");
		// 年
		short year = byteBuffer.getShort(142);
		map1.put("year", Short.toUnsignedInt(year) + "");
		// 经度 4
		float longitude = byteBuffer.getFloat(144);
		map1.put("longitude", String.valueOf(jd.format(longitude)));
		// 纬度 4
		float latitude = byteBuffer.getFloat(148);
		map1.put("latitude", String.valueOf(jd.format(latitude)));
		// 高度
		int height = byteBuffer.getInt(152);
		// map1.put("height", jd.format(Integer.toUnsignedString(height)));
		map1.put("height", String.valueOf(jd.format(height)));
		// 参考源 1
		byte referenceSource = byteBuffer.get(156);
		map1.put("referenceSource", Byte.toUnsignedInt(referenceSource) + "");
		// 同步状态 1
		byte synchronizationState = byteBuffer.get(157);
		map1.put("synchronizationState", Byte.toUnsignedInt(synchronizationState) + "");
		// 输出状态 1
		byte outState = byteBuffer.get(158);
		map1.put("outState", Byte.toUnsignedInt(outState) + "");
		// 时区选择 1
		byte hourChoose = byteBuffer.get(159);
		map1.put("hourChoose", Byte.toUnsignedInt(hourChoose) + "");
		// 同步精度 4
		int synchronizationAccuracy = byteBuffer.getInt(160);
		map1.put("synchronizationAccuracy", Integer.toUnsignedString(synchronizationAccuracy));
		// 北纬/南纬 1
		byte latitudeTips = byteBuffer.get(164);
		map1.put("latitudeTips", Byte.toUnsignedInt(latitudeTips) + "");
		// 东经/西经 1
		byte longitudeTips = byteBuffer.get(165);
		map1.put("longitudeTips", Byte.toUnsignedInt(longitudeTips) + "");
		// 校验和 1
		byte total = byteBuffer.get(166);
		map1.put("total", Byte.toUnsignedInt(total) + "");
		byteBuffer.position(167);
		// GPS数据备份 29
		byte[] byteGPSSys = new byte[29];
		byteBuffer.get(byteGPSSys);
		/* GPSend */
		// 发方节点号 1
		byte faNodeNo = byteBuffer.get(196);
		map1.put("faNodeNo", Byte.toUnsignedInt(faNodeNo) + "");
		byte souNodeNo = byteBuffer.get(197); // 收方节点号 1
		map1.put("receiveNodeNo", Byte.toUnsignedInt(souNodeNo) + "");
		short feedbackNo = byteBuffer.getShort(198); // 反馈指令序号 2
		// int feed = getNum(feedbackNo);
		map1.put("feedbackNo", Short.toUnsignedInt(feedbackNo) + radarString);
		short cmdStatus = byteBuffer.getShort(200); // 指令接收状态 2
		StringBuilder cmdRecive = getShort(cmdStatus);
		map1.put("cmdStatus", String.valueOf(cmdRecive));
		short taskNo = byteBuffer.getShort(202); // 任务编号 2
		map1.put("taskNo", Short.toUnsignedInt(taskNo) + radarString);
		short frontEndWorkTemperature = byteBuffer.getShort(204); // 前端工作温度 2
		map1.put("frontEndWorkTemperature", String.valueOf(df2.format(frontEndWorkTemperature * 0.0078125)));
		short fenjiWorkTemperature = byteBuffer.getShort(206); // 分机工作温度 2
		map1.put("fenjiWorkTemperature", String.valueOf(df2.format(fenjiWorkTemperature * 0.0078125)));
		/* 分机工作状态begin */
		long fenJiWorkStatus = byteBuffer.getLong(208); // 分机工作状态 8
		// StringBuilder stateWork = getWorkStates(fenJiWorkStatus);
		String stateWork = radarWorkState(fenJiWorkStatus);
		map1.put("fenJiWorkStatus", stateWork);
		/* 分机工作状态end */
		int channel1CxNum = byteBuffer.getInt(216); // 通道1(CX)全脉冲个数统计 4
		map1.put("channel1CxNum", Integer.toUnsignedString(channel1CxNum));
		int channel2ZcNum = byteBuffer.getInt(220); // 通道2(ZC)全脉冲个数统计 4
		map1.put("channel2ZcNum", Integer.toUnsignedString(channel2ZcNum));
		// 通道1(CX)辐射源个数统计 2
		short radiationChannel1CxNum = byteBuffer.getShort(224);
		map1.put("radiationChannel1CxNum", Short.toUnsignedInt(radiationChannel1CxNum) + radarString);
		// 通道2(ZC)辐射源个数统计 2
		short radiationChannel2ZcNum = byteBuffer.getShort(226);
		map1.put("radiationChannel2ZcNum", Short.toUnsignedInt(radiationChannel2ZcNum) + radarString);
		byte deviceNo = byteBuffer.get(228); // 设备编号
		// 设备编号
		String deviceNoString = Integer.toBinaryString((deviceNo & 0xFF) + 0x100).substring(1);
		StringBuilder stringBuilder1 = new StringBuilder(deviceNoString);
		stringBuilder1.reverse();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(stringBuilder1, 0, 4);
		int bit7_4 = Integer.parseInt(stringBuilder1.substring(4, 6));
		// bit7-bit4 0：520项目 1：西沙改 2：大车 3：舰载
		if (bit7_4 == 0) {
			stringBuilder.append("0");
		} else if (bit7_4 == 1) {
			stringBuilder.append("1");
		} else if (bit7_4 == 10) {
			stringBuilder.append("2");
		} else if (bit7_4 == 11) {
			// 3在2进制中为11
			stringBuilder.append("3");
		}
		map1.put("deviceNo", stringBuilder.toString());
		byteBuffer.position(229);

		// 备份 3
		byte[] backups3 = new byte[3];
		byteBuffer.get(backups3);

		// 分机控制状态 - 看门狗、合路选择
		short defendState = byteBuffer.getShort(232);
		String fenji = getFenjiState(defendState);
		map1.put("defendState", fenji);
		byteBuffer.position(234);
		// 备份 2
		byte[] backups2 = new byte[2];
		byteBuffer.get(backups2);
		// 6-18HHz长电缆均衡衰减控制1 1
		byte hhzz6_18_1 = byteBuffer.get(236);
		map1.put("hhzz6_18_1", Byte.toUnsignedInt(hhzz6_18_1) + radarString);
		// 6-18HHz长电缆均衡衰减控制2 1
		byte hhzz6_18_2 = byteBuffer.get(237);
		map1.put("hhzz6_18_2", Byte.toUnsignedInt(hhzz6_18_2) + radarString);
		// 通道2（ZC）中频衰减 1
		byte channel2ZcMidCut = byteBuffer.get(238);
		map1.put("channel2ZcMidCut", Byte.toUnsignedInt(channel2ZcMidCut) + radarString);
		// 通道1（CX）中频衰减 1
		byte channel1CxMidCut = byteBuffer.get(239);
		map1.put("channel1CxMidCut", Byte.toUnsignedInt(channel1CxMidCut) + radarString);
		/* 前端begin */
		// 前端包头 2
		short frontHeader = byteBuffer.getShort(240); // 6863 ---1acf
		map1.put("frontHeader", Integer.toHexString((int) frontHeader));
		// 前端序号 2
		short heardBeforeNum = byteBuffer.getShort(242);
		map1.put("heardBeforeNum", Short.toUnsignedInt(heardBeforeNum) + radarString);
		// MARK地址 2
		short markAddress = byteBuffer.getShort(244);
		map1.put("markAddress", Short.toUnsignedInt(markAddress) + radarString);
		// 通告方式 2
		short style = byteBuffer.getShort(246);
		map1.put("style", Short.toUnsignedInt(style) + radarString);
		// 故障检测 1
		byte faultDetection = byteBuffer.get(248);
		map1.put("faultDetection", errorCheck(faultDetection));
		// (雷达)本振锁定指示 1
		byte locking = byteBuffer.get(249);
		map1.put("locking", getLockingInfo(locking));
		// 前端温度 2
		short frontEndWorkTemperature2 = byteBuffer.getShort(250); // 30度
		map1.put("frontEndWorkTemperature2", df2.format(frontEndWorkTemperature2 * 0.0078125));
		// FPGA计数 1
		byte fpga1 = byteBuffer.get(252);
		map1.put("fpga1", Byte.toUnsignedInt(fpga1) + radarString);
		// 指令状态 1
		byte state1 = byteBuffer.get(253);
		map1.put("state1", getCommondState(state1));
		// 工作频率 2
		short workPrence = byteBuffer.getShort(254);
		map1.put("workPrence", Short.toUnsignedInt(workPrence) + radarString);
		byteBuffer.position(256);
		// 备份 12
		byte[] backupss = new byte[12];
		byteBuffer.get(backupss);

		// 微波-CRC 2
		short microwaveCRC = byteBuffer.getShort(268);
		map1.put("microwaveCRC", Short.toUnsignedInt(microwaveCRC) + radarString);
		// 前端包尾 2
		short frontTail = byteBuffer.getShort(270);
		map1.put("frontTail", Integer.toHexString(Short.toUnsignedInt(frontTail)));
		// byteBuffer.position(272);
		/* 前端end */

		/* 关键状态begin */
		// 通道1K7A生成PDW个数 4
//		int a7k1=byteBuffer.getInt(272);
//		map1.put("a7k1", Integer.toUnsignedString(a7k1));
//		// 通道1K7A输出原始PDW个数 4
//		int a7k1PDW=byteBuffer.getInt(276);
//		// 分选K7接收原始PDW个数 4
//		int a7DW=byteBuffer.getInt(280);
//		// 分选K7排序后输出PDW个数 4
//		int k7=byteBuffer.getInt(284);
//		// 分选K7给DSP的PDW个数 4
//		int k7DSP=byteBuffer.getInt(288);
//		// 分选DSP接收的PDW个数 4
//		int dsp=byteBuffer.getInt(292);
//		// 分选DSP上传通道1的PDW个数 4
//		int dspPDW=byteBuffer.getInt(296);
//		// 分选K7上传通道1的PDW个数 4
//		int k7pdw=byteBuffer.getInt(300);
//		// 主控K7接收通道1上传的PDW个数 4
//		int k7UpPdw=byteBuffer.getInt(304);
//		// 主控DSP接收通道1的PDW个数 4
//		int dspRecivePDW=byteBuffer.getInt(308);
//		// 通道2 K7A生成PDW个数 4
//		int createK7APDW=byteBuffer.getInt(312);
//		// 通道2 K7A输出原始PDW个数 4
//		int outPutK7APDW=byteBuffer.getInt(316);
//		// 主控K7接收原始PDW个数 4
//		int reciveK7APDW=byteBuffer.getInt(320);
//		// 主控K7排序后输出PDW个数 4
//		int masterControlK7PDW=byteBuffer.getInt(324);
//		// 主控K7给DSP的PDW个数 4
//		int masterControlK7SendPDW=byteBuffer.getInt(328);
//		// 主控DSP接收的PDW个数 4
//		int masterControlDSPRecivePDW=byteBuffer.getInt(332);
//		// 主控DSP网络上传的PDW个数 4  //68 340
//		int masterControlDSPUploadPDW=byteBuffer.getInt(336);
//		// 通道1频谱状态</ 1
//		byte frequencySpectrumState1=byteBuffer.get(340);
//		// 通道2频谱状态 1
//		byte frequencySpectrumState2=byteBuffer.get(341);
//		byteBuffer.position(342);
//		// 备份 2
//		byte [] hingeState=new byte[2];
//		// 分选dSP上传的辐射源个数 1
//		byte sizingCountDSP=byteBuffer.get(346);
//		// 分选K7辐射源状态 1
//		byte sizingStateK7=byteBuffer.get(347);
//		// 主控DSP上传辐射源个数 1
//		byte masterControlDSPCount=byteBuffer.get(348);
//		byteBuffer.position(349);
//		// 备份 1
//		byte [] hingeState2=new byte[1];
//		// 分选发送的引导辐射源批号 1
//		byte sizingSendNum=byteBuffer.get(350);
//		// 通道1Z7_PL状态 1
//		byte thoroughfareZ7PL=byteBuffer.get(351);
//		// 通道1引导辐射源状态 1
//		byte thoroughfareState=byteBuffer.get(352);
//		// 通道1脉内引导辐射源带宽 1
//		byte thoroughfareBrand=byteBuffer.get(353);
//		// 通道1脉内引导辐射源批次号 1
//		byte thoroughfareNum=byteBuffer.get(354);
//		// 通道1脉内分析结果个数 1
//		byte thoroughfareResultCount=byteBuffer.get(355);
//		// 通道1脉间和信道化脉内类型 1
//		byte thoroughfareInterpulseAndType=byteBuffer.get(356);
//		// 通道1脉内类型 1 
//		byte thoroughfareType=byteBuffer.get(357);
//		// 通道1脉内载频值 4
//		int thoroughfareFrequency=byteBuffer.getInt(358);
//		// 通道1脉内中心频率 4
//		int thoroughfareAmong=byteBuffer.getInt(362);
//		// 通道1脉内分析结果个数 1
//		byte thoroughfareResultNum=byteBuffer.get(366);
//		// 分选接收到的脉内信息1    366
//		byte sizingReviceMessage=byteBuffer.get(367);
//		byteBuffer.position(368);
//		// 备份 3
//		byte[] importendState = new byte[3];
//		byteBuffer.get(importendState);
//		//byteBuffer.position(369);
//
//		// 主控发送的引导辐射源批号 1
//		byte masterControlSendNUm=byteBuffer.get(371);
//		// 通道2Z7_PL状态 1
//		byte thoroughfareZ7PLTwo=byteBuffer.get(369);
//		// 通道2引导辐射源状态 1
//		byte thoroughfareStateTwo=byteBuffer.get(370);
//		// 通道2脉内引导辐射源带宽 1
//		byte thoroughfareBrandTwo=byteBuffer.get(371);
//		// 通道2脉内引导辐射源批次号1
//		byte thoroughfareNumTwo=byteBuffer.get(372);
//		// 通道2脉内分析结果个数 1
//		byte thoroughfareResultCountTwo=byteBuffer.get(373);
//		// 通道2脉间和信道化脉内类型 1
//		byte thoroughfareInterpulseAndTypeTwo=byteBuffer.get(374);
//		// 通道2脉内类型 1
//		byte thoroughfareTypeTwo=byteBuffer.get(375);
//		// 通道2脉内载频值 4
//		int thoroughfareFrequencyTwo=byteBuffer.getInt(376);
//		// 通道2脉内中心频率</ 4  
//		int thoroughfareAmongTwo=byteBuffer.getInt(380);
//		// 主控接收到的脉内信息 1
//		byte masterControlReciveMessage=byteBuffer.get(384);
//		byteBuffer.position(385);
//		// 备份 3
//		byte [] hingeState3=new byte[3];
//		// 通道1K7心跳 1
//		byte thoroughfareK7One=byteBuffer.get(388);
//		// 通道1Z7心跳 1
//		byte thoroughfareZ7One=byteBuffer.get(389);
//		// 通道2K7心跳 1
//		byte thoroughfareK7Two=byteBuffer.get(390);
//		// 通道2Z7心跳 1
//		byte thoroughfareZ7Two=byteBuffer.get(391);
//		// 分选板心跳 1
//		byte sizingHeartBeat=byteBuffer.get(392);
//		// 主控板心跳 1
//		byte masterControlHeartBeat=byteBuffer.get(393);
//		byteBuffer.position(394);
//		// 备份 6      
//		// (32字节)
//		byte[] access = new byte[6];
//		byteBuffer.get(access);
		/* 电源状态 */
		// 包头 2 CF1A 400是正确的
		short electricSource = byteBuffer.getShort(400);
		map1.put("electricSource", Integer.toHexString(Short.toUnsignedInt(electricSource)));
		// +48V电压 2 voltage ,voltage ,plus minus
		short voltagelPlus48 = byteBuffer.getShort(402);
		map1.put("voltagelPlus48", getBoardTemp(voltagelPlus48));
		// +48V电流 2
		short anpeiPlus48 = byteBuffer.getShort(404);
		map1.put("anpeiPlus48", getBoardTemp(anpeiPlus48));
		// >+A12V(1)电压(计算机)</ 2
		short voltagePlus12 = byteBuffer.getShort(406);
		map1.put("voltagePlus12", getBoardTemp(voltagePlus12));
		// +A12V(1)电流 2
		short anpeiPlus12 = byteBuffer.getShort(408);
		map1.put("anpeiPlus12", getBoardTemp(anpeiPlus12));
		// +D12V电压 2
		short d12V = byteBuffer.getShort(410);
		map1.put("d12V", getBoardTemp(d12V));
		// +D12V电流 2
		short anpeiD12V = byteBuffer.getShort(412);
		map1.put("anpeiD12V", getBoardTemp(anpeiD12V));
		// >+A18V(1)电压 2
		short votageA18V = byteBuffer.getShort(414);
		map1.put("votageA18V", getBoardTemp(votageA18V));
		// >+A18V(1)电流 2
		short anpeiA18V = byteBuffer.getShort(416);
		map1.put("anpeiA18V", getBoardTemp(anpeiA18V));
		// +A18V(2)电压 2
		short vatageA18V2 = byteBuffer.getShort(418);
		map1.put("vatageA18V2", getBoardTemp(vatageA18V2));
		// +A18V(2)电流 2
		short anpeiA18V2 = byteBuffer.getShort(420);
		map1.put("anpeiA18V2", getBoardTemp(anpeiA18V2));
		// +A12V(2)电压(微波) 2
		short vatageA12V2 = byteBuffer.getShort(422);
		map1.put("vatageA12V2", getBoardTemp(vatageA12V2));
		// +A12V(2)电流 2
		short anpeiA12V2 = byteBuffer.getShort(424);
		map1.put("anpeiA12V2", getBoardTemp(anpeiA12V2));
		// >+D5V电压</ 2
		short vategeD5V = byteBuffer.getShort(426);
		map1.put("vategeD5V", getBoardTemp(vategeD5V));
		// +D5V电流 2
		short anpeiD5V = byteBuffer.getShort(428);
		map1.put("anpeiD5V", getBoardTemp(anpeiD5V));

		// >-5V电压 2
		short vatageMinus5V = byteBuffer.getShort(430);
		map1.put("vatageMinus5V", getBoardTemp(vatageMinus5V));
		// -5V电流 2
		short anpeiMinus5V = byteBuffer.getShort(432);
		map1.put("anpeiMinus5V", getBoardTemp(anpeiMinus5V));
		// +A6V电压 2
		short vatagePlusA6V = byteBuffer.getShort(434);
		map1.put("vatagePlusA6V", getBoardTemp(vatagePlusA6V));
		// +A6V电流 2
		short anpeiPlusA6V = byteBuffer.getShort(436);
		map1.put("anpeiPlusA6V", getBoardTemp(anpeiPlusA6V));
		// +24V电流 2
		short anpeiPlus24V = byteBuffer.getShort(438);
		map1.put("anpeiPlus24V", getBoardTemp(anpeiPlus24V));
		// +24V电压 2
		short vatagePlus24V = byteBuffer.getShort(440);
		map1.put("vatagePlus24V", getBoardTemp(vatagePlus24V));
		// 包尾 2 1DCF
		short electricEnd = byteBuffer.getShort(442);
		map1.put("electricEnd", Integer.toHexString(Short.toUnsignedInt(electricEnd)));
		// (44)

		// 结尾
		int verify = byteBuffer.getInt(444); // 校验和
		int messageEnd = byteBuffer.getInt(448); // 报文尾
		map1.put("host", host);
		simpMessagingTemplate.convertAndSend("/uploadRadarSubSystemWorkStatusMessage/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
	}

	/**
	 * 敌我系统工作标签包
	 */
	@Async
	public void uploadEnemyAndUsPackageMessage(ByteBuffer byteBuffer, String host) {
		byteBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		Map<String, Number> map = receiveFixedInformation(byteBuffer);
		Map<String, String> map1 = new HashMap<>();
		int messageLength = byteBuffer.getInt(48);
		Long taskFlowNo = byteBuffer.getLong(52);
		// 敌我系统状态信息
		short header = byteBuffer.getShort(60); // 帧有效标记0x1ACF
		short dataType = byteBuffer.getShort(62);// 数据类型
		int dataLength = byteBuffer.getInt(64);// 数据长度
		// byteBuffer.position(68);
		/**
		 * 系统控制信息
		 */
		short hearderInformation = byteBuffer.getShort(68); // 包头 53F8
		map1.put("hearderInformation", Integer.toHexString(Short.toUnsignedInt(hearderInformation)));
		short systemControl = byteBuffer.getShort(70); // 信息包序号
		map1.put("systemControl", Short.toUnsignedInt(systemControl) + radarString);
		// TODO 时间码
		long timeCodings = byteBuffer.getLong(72);
		String timeString = longToBinaryString(timeCodings);

		String resultString = "";
		// 年
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(0, 16), 2)) + "/";
		// 月
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(16, 24), 2)) + "/";
		// 日
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(24, 32), 2)) + " ";
		// 时
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(56, 64), 2)) + ":";
		// 分
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(48, 56), 2)) + ":";
		// 秒
		resultString = resultString + String.valueOf(Short.parseShort(timeString.substring(32, 48), 2));
		map1.put("timeCodings", resultString);
		byteBuffer.position(80);
		byte[] byte1 = new byte[52]; // 备份
		byteBuffer.get(byte1);
		/**
		 * GPS数据
		 */
		int hearderGPS = byteBuffer.getInt(132); // GPS包头 01728d33
		map1.put("hearderGPS", Integer.toHexString(hearderGPS));
		byte hour = byteBuffer.get(136); // 时
		map1.put("hour", Byte.toUnsignedInt(hour) + radarString);
		byte mini = byteBuffer.get(137); // 分
		map1.put("mini", Byte.toUnsignedInt(mini) + radarString);
		short second = byteBuffer.getShort(138); // 秒
		map1.put("second", Short.toUnsignedInt(second) + radarString);
		byte days = byteBuffer.get(140); // 日
		map1.put("days", Byte.toUnsignedInt(days) + radarString);
		byte mouths = byteBuffer.get(141); // 月
		map1.put("mouths", Byte.toUnsignedInt(mouths) + radarString);
		short year = byteBuffer.getShort(142); // 年
		map1.put("year", Short.toUnsignedInt(year) + radarString);
		float longitude = byteBuffer.getFloat(144); // 经度(保留小数点后4位)
		// 解析
		map1.put("longitude", String.valueOf(jd.format(longitude)));
		float latitude = byteBuffer.getFloat(148); // 纬度
		map1.put("latitude", String.valueOf(jd.format(latitude)));
		int height = byteBuffer.getInt(152); // 高度
		map1.put("height", String.valueOf(jd.format(height)));
		byte referenceSource = byteBuffer.get(156); // 参考源
		map1.put("referenceSource", Byte.toUnsignedInt(referenceSource) + radarString);
		byte synchronizationState = byteBuffer.get(157); // 同步状态
		map1.put("synchronizationState", Byte.toUnsignedInt(synchronizationState) + radarString);
		byte outState = byteBuffer.get(158); // 输出状态
		map1.put("outState", Byte.toUnsignedInt(outState) + radarString);
		byte hourChoose = byteBuffer.get(159); // 时区选择
		map1.put("hourChoose", Byte.toUnsignedInt(hourChoose) + radarString);
		int synchronizationAccuracy = byteBuffer.getInt(160); // 同步精度
		map1.put("synchronizationAccuracy", Integer.toUnsignedString(synchronizationAccuracy));
		byte latitudeTips = byteBuffer.get(164); // 纬度提示
		map1.put("latitudeTips", Byte.toUnsignedInt(latitudeTips) + radarString);
		byte longitudeTips = byteBuffer.get(165); // 经度提示
		map1.put("longitudeTips", Byte.toUnsignedInt(longitudeTips) + radarString);
		byte validationTotal = byteBuffer.get(166); // 校验和
		map1.put("validationTotal", Byte.toUnsignedInt(validationTotal) + radarString);
		byteBuffer.position(167);
		byte[] backupsGPS = new byte[29]; // GPS数据备份
		byteBuffer.get(backupsGPS);

		// 敌我系统状态
		// 数据信息
		byte systemWorkState = byteBuffer.get(196);// 系统工作状态
		map1.put("systemWorkState", getSystemWorkState(systemWorkState));
		byte receiveInstruction = byteBuffer.get(197); // 接收指令计数
		map1.put("receiveInstruction", Byte.toUnsignedInt(receiveInstruction) + radarString);
		++receiveInstruction;

		/** 分机计数begin */
		byte extensionCount = byteBuffer.get(198);
		map1.put("extensionCount", getZhuKongJiShuNum1(extensionCount));
		/** 解析分机计数end */
		/** 分机计数begin */
		byte extensionCount1 = byteBuffer.get(199);
		map1.put("extensionCount1", getZhuKongJiShuNum2(extensionCount1));
		/** 分机计数end */
		byteBuffer.position(200);
		byte[] byte2 = new byte[4]; // 备份
		byteBuffer.get(byte2);
		short frontEndOperatingTemperature = byteBuffer.getShort(204); // 前端工作温度
		map1.put("frontEndOperatingTemperature",
				df2.format(Short.toUnsignedInt(frontEndOperatingTemperature) * 0.0078125));
		short mainControlWorkingTemperature = byteBuffer.getShort(206); // 主控工作温度
		map1.put("mainControlWorkingTemperature",
				df2.format(Short.toUnsignedInt(mainControlWorkingTemperature) * 0.01));
		short detectionOfWorkingTemperature = byteBuffer.getShort(208); // 检测工作温度
		map1.put("detectionOfWorkingTemperature",
				df2.format(Short.toUnsignedInt(detectionOfWorkingTemperature) * 0.01));
		byteBuffer.position(210);
		byte[] bytes3 = new byte[10]; // 备份(10)
		byteBuffer.get(bytes3);
		int numberOfFullPulse740 = byteBuffer.getInt(220);// 740全脉冲个数
		map1.put("numberOfFullPulse740", Integer.toUnsignedString(numberOfFullPulse740));
		int numberOfFullPulse837_5 = byteBuffer.getInt(224);// 837.5全脉冲个数(两个8字节)
		map1.put("numberOfFullPulse837_5", Integer.toUnsignedString(numberOfFullPulse837_5));

		int PDW1030 = byteBuffer.getInt(228);// 1030PDW个数（4）
		map1.put("PDW1030", Integer.toUnsignedString(PDW1030));
		int PDW1059 = byteBuffer.getInt(232);// 1059PDW个数（4）
		map1.put("PDW1059", Integer.toUnsignedString(PDW1059));
		int PDW1090 = byteBuffer.getInt(236);// 1090PDW个数（4）
		map1.put("PDW1090", Integer.toUnsignedString(PDW1090));
		int PWD1464 = byteBuffer.getInt(240);// 1464PDW个数（4）
		map1.put("PWD1464", Integer.toUnsignedString(PWD1464));
		int PWD1532 = byteBuffer.getInt(244);// 1532PDW个数（4）
		map1.put("PWD1532", Integer.toUnsignedString(PWD1532));

		long enemyAndUs740 = byteBuffer.getInt(248);// 740IFF识别个数(8)
		map1.put("enemyAndUs740", Long.toUnsignedString(enemyAndUs740));
		int enemyAndUs1030 = byteBuffer.getInt(256);// 1030IFF识别个数（4）
		map1.put("enemyAndUs1030", Integer.toUnsignedString(enemyAndUs1030));
		int IFF1090 = byteBuffer.getInt(260); // 1090IFF个数（4）
		map1.put("IFF1090", Integer.toUnsignedString(IFF1090));
		int IFF1464 = byteBuffer.getInt(264);// 1464IFF个数（4）
		map1.put("IFF1464", Integer.toUnsignedString(IFF1464));
		int IFF1532 = byteBuffer.getInt(268);// 1532IFF个数（4）
		map1.put("IFF1532", Integer.toUnsignedString(IFF1532));
		byteBuffer.position(272);
		byte[] mid1030 = new byte[28];// 1030中频个数（28）
		byteBuffer.get(mid1030);

		int M51030 = byteBuffer.getInt(300);// 1030 M5个数（4）
		map1.put("M51030", Integer.toUnsignedString(M51030));
		int M51090 = byteBuffer.getInt(304);// 1090 M5个数（4）
		map1.put("M51090", Integer.toUnsignedString(M51090));
		long midM51030 = byteBuffer.getLong(308);// 1030 M5中频个数（8）
		map1.put("midM51030", Long.toUnsignedString(midM51030));
		byteBuffer.position(316);
		byte[] bytes4 = new byte[154]; // 电源状态(154)
		byteBuffer.get(bytes4);
		byte masterControlFPGA1 = byteBuffer.get(470); // 主控FPGA1版本号（1）
		map1.put("masterControlFPGA1", df2.format(Byte.toUnsignedInt(masterControlFPGA1) * 0.01));
		byte masterControlFPGA2S = byteBuffer.get(471);// 主控FPGA2版本号（1）
		map1.put("masterControlFPGA2S", df2.format(Byte.toUnsignedInt(masterControlFPGA2S) * 0.01));

		byte masterControlDSP = byteBuffer.get(472); // 主控DSP版本号（1）
		Integer masterDsp = getDsp(masterControlDSP);
		map1.put("masterControlDSP", String.valueOf(df2.format(masterDsp * 0.01)));
		byte testingFPGA1 = byteBuffer.get(473);// 检测FPGA1版本号（1）
		Integer testing = getDsp(testingFPGA1);
		map1.put("testingFPGA1", String.valueOf(df2.format(testing * 0.01)));
		byte testingFPGA2 = byteBuffer.get(474);// 检测FPGA2版本号（1）
		Integer testingF2 = getDsp(testingFPGA2);
		map1.put("testingFPGA2", String.valueOf(df2.format(testingF2 * 0.01)));
		byte testingDSP = byteBuffer.get(475);// 检测DSP版本号（1）
		Integer test2 = getDsp(testingDSP);
		map1.put("testingDSP", String.valueOf(df2.format(test2 * 0.01)));
		byteBuffer.position(476);
		byte[] bytes5 = new byte[16]; // 版本号（16）
		byteBuffer.get(bytes5);
		String masterControlIp = getToIp(byteBuffer.getInt(492));// 主控IP地址（4）
		map1.put("masterControlIp", masterControlIp);
		String testingIp = getToIp(byteBuffer.getInt(496)); // 检测IP地址（4）
		map1.put("testingIp", testingIp);
		String dataTransmissionIp2 = getToIp(byteBuffer.getInt(500));// 数传2 IP地址（4）
		map1.put("dataTransmissionIp2", dataTransmissionIp2);
		String dataTransmissionIp3 = getToIp(byteBuffer.getInt(504));// 数传3 IP地址3（4）
		map1.put("dataTransmissionIp3", dataTransmissionIp3);
		short masterDSP = byteBuffer.getShort(508);// 主控DSP端口号（2）
		map1.put("masterDSP", Short.toUnsignedInt(masterDSP) + radarString);
		short internalInstructionSide = byteBuffer.getShort(510);// 内部指令端口号（2）
		map1.put("internalInstructionSide", Short.toUnsignedInt(internalInstructionSide) + radarString);
		byteBuffer.position(512);
		byte[] bytes6 = new byte[4]; // 备份（4）
		byteBuffer.get(bytes6);

		// 主控MAC地址
		String masterControlMAC = getMac(byteBuffer.getShort(516), byteBuffer.getInt(518));
		map1.put("masterControlMAC", masterControlMAC);
		// 检测MAC地址（6）
		String testingMAC = getMac(byteBuffer.getShort(522), byteBuffer.getInt(524));
		map1.put("testingMAC", testingMAC);
		// 数传2 MAC地址（6）
		String dataTransmissionMAC2 = getMac(byteBuffer.getShort(528), byteBuffer.getInt(530));
		map1.put("dataTransmissionMAC2", dataTransmissionMAC2);
		// 数传3MAC地址（6）
		String dataTransmissionMAC3 = getMac(byteBuffer.getShort(534), byteBuffer.getInt(536));
		map1.put("dataTransmissionMAC3", dataTransmissionMAC3);
		byteBuffer.position(540);
		byte[] bytes7 = new byte[16]; // 备份(16)
		byteBuffer.get(bytes7);

		String masterComputerIp = getToIp(byteBuffer.getInt(556)); // 主控上位机IP（4）
		map1.put("masterComputerIp", masterComputerIp);
		String dataTransmissionIP1 = getToIp(byteBuffer.getInt(560)); // 数传1上位机IP（4）
		map1.put("dataTransmissionIP1", dataTransmissionIP1);
		String dataTransmissionIP2 = getToIp(byteBuffer.getInt(564)); // 数传2上位机IP（4）
		map1.put("dataTransmissionIP2", dataTransmissionIP2);
		String dataTransmissionIP3 = getToIp(byteBuffer.getInt(568));// 数传3上位机IP（4）
		map1.put("dataTransmissionIP3", dataTransmissionIP3);

		short labelPackage = byteBuffer.getShort(572);// 标签包端口号（2）
		map1.put("labelPackage", Short.toUnsignedInt(labelPackage) + radarString);
		short dataPort1 = byteBuffer.getShort(574); // 数据端口号1（2）
		map1.put("dataPort1", Short.toUnsignedInt(dataPort1) + radarString);
		short dataPort2 = byteBuffer.getShort(576); // 数据端口号2（2）
		map1.put("dataPort2", Short.toUnsignedInt(dataPort2) + radarString);
		short dataPort3 = byteBuffer.getShort(578);// 数据端口号3（2）
		map1.put("dataPort3", Short.toUnsignedInt(dataPort3) + radarString);

		String internalStateIP = getToIp(byteBuffer.getInt(580));// 内部状态IP（4）
		map1.put("internalStateIP", internalStateIP);
		short internalStatePort = byteBuffer.getShort(584);// 内部状态端口号（2）
		map1.put("internalStatePort", Short.toUnsignedInt(internalStatePort) + radarString);
		byteBuffer.position(586);
		byte[] bytes11 = new byte[2];// 备份(2)
		byteBuffer.get(bytes11);
		/*
		 * byteBuffer.position(557); byte [] frontEndState=new byte[32];
		 * byteBuffer.get(frontEndState);
		 */
		/**
		 * 前端状态
		 */
		short anteriorSegmentHearder = byteBuffer.getShort(588); // 前端包头（不显示）(2)/1ACF
		// map1.put("anteriorSegmentHearder",
		// Short.toUnsignedInt(anteriorSegmentHearder) + radarString);
		String hexString = Integer.toHexString(Short.toUnsignedInt(anteriorSegmentHearder));
		map1.put("anteriorSegmentHearder", hexString);
		short anteriorSegmentNum = byteBuffer.getShort(590); // 前端序号（不显示）(2)
		map1.put("anteriorSegmentNum", Short.toUnsignedInt(anteriorSegmentNum) + radarString);
		short addressMARK = byteBuffer.getShort(592); // MARK地址（不显示）(2)
		map1.put("addressMARK", Short.toUnsignedInt(addressMARK) + radarString);
		short noticeStyle = byteBuffer.getShort(594); // 通知方式（不显示）(2)
		map1.put("noticeStyle", Short.toUnsignedInt(noticeStyle) + radarString);
		// 频点1,2故障检测(1)
		byte frequencyPointFault1_2 = byteBuffer.get(596);
		StringBuilder stringBuilder1_2 = getByte(frequencyPointFault1_2);
		map1.put("frequencyPointFault1_2", String.valueOf(stringBuilder1_2));

		// 频点3,4故障检测输出(1)
		byte frequencyPointFault3_4 = byteBuffer.get(597);
		map1.put("frequencyPointFault3_4", String.valueOf(frequencyPointFault3_4));
		// 频点5,6故障检测(1)
		byte frequencyPointFault5_6 = byteBuffer.get(598);
		StringBuilder stringBuilder5_6_1 = getByte(frequencyPointFault5_6);
		map1.put("frequencyPointFault5_6", String.valueOf(stringBuilder5_6_1));

		// 频点7,8故障检测(1)
		byte frequencyPointFault7_8 = byteBuffer.get(599);
		StringBuilder stringBuilder7_8_1 = getByte(frequencyPointFault7_8);
		map1.put("frequencyPointFault7_8", String.valueOf(stringBuilder7_8_1));
		// 采样时钟(1)
		byte samplingClock = byteBuffer.get(600);
		map1.put("samplingClock", getSamplingClock(samplingClock));
		// 本帧锁定指示
		byte locking = byteBuffer.get(601);
		StringBuilder frameLocking2_1 = getByte(locking);
		map1.put("locking", String.valueOf(frameLocking2_1));

		// 自检源锁定指示(1)
		byte selfLocking = byteBuffer.get(602);
		map1.put("selfLocking", Byte.toUnsignedInt(selfLocking) + radarString);
		// 微波模块工作温度(2)
		short workingTemp = byteBuffer.getShort(603);
		map1.put("workingTemp", df2.format(Short.toUnsignedLong(workingTemp) * 0.0078125));
		// FPGA计数(1)
		byte beforNumFPGA = byteBuffer.get(605);
		map1.put("beforNumFPGA", Byte.toUnsignedInt(beforNumFPGA) + radarString);
		// 自检源频点选择(1)
		byte selfPointChoose = byteBuffer.get(606);
		map1.put("selfPointChoose", Byte.toUnsignedInt(selfPointChoose) + radarString);
		// 敌我前端工作模式(1)
		byte beforEnemyAndUsWork = byteBuffer.get(607);
		map1.put("beforEnemyAndUsWork", Byte.toUnsignedInt(beforEnemyAndUsWork) + radarString);
		byteBuffer.position(608);
		byte[] beforByte = new byte[8]; // 备份(8)
		byteBuffer.get(beforByte);
		// 前端crc
		short crc = byteBuffer.getShort(616);
		// 包尾
		short tail = byteBuffer.getShort(618);
		map1.put("tail", Integer.toHexString(Short.toUnsignedInt(tail)));
		/**
		 * 关键状态信息(敌我关键状态)begin
		 */
		byte numFPGA1 = byteBuffer.get(620);// FPGA1计数(1)
		map1.put("numFPGA1", Byte.toUnsignedInt(numFPGA1) + "");
		short numFPGA1Brodcast = byteBuffer.getShort(621);// FPGA1广播计数(1)
		map1.put("numFPGA1Brodcast", Short.toUnsignedInt(numFPGA1Brodcast) + "");
		byte clockFPGA1State = byteBuffer.get(623);// FPGA1时钟状态(1)
		map1.put("clockFPGA1State", Byte.toUnsignedInt(clockFPGA1State) + "");
		short numFPGA1AD = byteBuffer.getShort(624);// FPGA1 AD量化值(2)
		map1.put("numFPGA1AD", Short.toUnsignedInt(numFPGA1AD) + "");
		int num1030PDW = byteBuffer.getInt(626);// 1030PDW个数(4)
		map1.put("num1030PDW", Integer.toUnsignedString(num1030PDW) + "");
		int num1030M5PWD = byteBuffer.getInt(630);// 1030M5 PWD个数(4)
		map1.put("num1030M5PWD", Integer.toUnsignedString(num1030M5PWD) + "");
		short num1030IFF = byteBuffer.getShort(634);// 1030IFF个数(2)
		map1.put("num1030IFF", Short.toUnsignedInt(num1030IFF) + "");
		short num1030M5IFF = byteBuffer.getShort(636);// 1030M5 IFF个数(2)
		map1.put("num1030M5IFF", Short.toUnsignedInt(num1030M5IFF) + ""); // (18)

		byte numFPGA2 = byteBuffer.get(638);// FPGA2个数
		map1.put("numFPGA2", Byte.toUnsignedInt(numFPGA2) + "");
		short numFPGA2Brodcast = byteBuffer.getShort(639);// FPGA2广播计数
		map1.put("numFPGA2Brodcast", Short.toUnsignedInt(numFPGA2Brodcast) + radarString);
		byte clockFPGA2State = byteBuffer.get(641);// FPGA2时钟状态
		map1.put("clockFPGA2State", Byte.toUnsignedInt(clockFPGA2State) + radarString);
		short numpFPGA2AD = byteBuffer.getShort(642);// FPGA2 AD量化值
		map1.put("numpFPGA2AD", Short.toUnsignedInt(numpFPGA2AD) + radarString);
		int num1090 = byteBuffer.getInt(644);// 1090PDW个数
		map1.put("num1090", Integer.toUnsignedString(num1090));
		int num1090M5PDW = byteBuffer.getInt(648);// 1090M5PDW个数
		map1.put("num1090M5PDW", Integer.toUnsignedString(num1090M5PDW));
		short num1090IFF = byteBuffer.getShort(652);// 1090IFF个数
		map1.put("num1090IFF", Short.toUnsignedInt(num1090IFF) + radarString);
		short num1090M5IFF = byteBuffer.getShort(654);// 1090M5 IFF个数
		map1.put("num1090M5IFF", Short.toUnsignedInt(num1090M5IFF) + radarString); // （18）

		short informationDSPNum = byteBuffer.getShort(656);// DSP信息包序号
		map1.put("informationDSPNum", Short.toUnsignedInt(informationDSPNum) + radarString);
		short cardWorkingTem = byteBuffer.getShort(658);// 卡板工作温度
		map1.put("cardWorkingTem", Short.toUnsignedInt(cardWorkingTem) + radarString);
		int numDSP1030 = byteBuffer.getInt(660);// DSP 1030全脉冲个数
		map1.put("numDSP1030", Integer.toUnsignedString(numDSP1030));
		int numDSP1090 = byteBuffer.getInt(664);// 1DSP 1090全脉冲个数
		map1.put("numDSP1090", Integer.toUnsignedString(numDSP1090));
		int numEnemyAndUs1030 = byteBuffer.getInt(668);// DSP 1030敌我个数
		map1.put("numEnemyAndUs1030", Integer.toUnsignedString(numEnemyAndUs1030));
		int numEnemyAndUs1090 = byteBuffer.getInt(672);// DSP 1090敌我个数
		map1.put("numEnemyAndUs1090", Integer.toUnsignedString(numEnemyAndUs1090));
		int numEnemyAndUsM51030 = byteBuffer.getInt(676);// DSP 1030M5敌我个数
		map1.put("numEnemyAndUsM51030", Integer.toUnsignedString(numEnemyAndUsM51030));
		int numEnemyAndUsM51090 = byteBuffer.getInt(680);// DSP1090M5个数
		map1.put("numEnemyAndUsM51090", Integer.toUnsignedString(numEnemyAndUsM51090)); // （28）
		// 检测板
		byte numTestFPGA1 = byteBuffer.get(684);// FPGA1计数
		map1.put("numTestFPGA1", Byte.toUnsignedInt(numTestFPGA1) + "");
		short brodcastTestFPGA1 = byteBuffer.getShort(685);// FPGA1广播计数
		map1.put("brodcastTestFPGA1", Short.toUnsignedInt(brodcastTestFPGA1) + radarString);
		byte numTestFPGA2 = byteBuffer.get(687);// FPGA2个数
		map1.put("numTestFPGA2", Byte.toUnsignedInt(numTestFPGA2) + "");
		short numtTestBrodcastFPGA2 = byteBuffer.getShort(688);// FPGA2广播计数
		map1.put("numtTestBrodcastFPGA2", Short.toUnsignedInt(numtTestBrodcastFPGA2) + radarString);
		byte clockTestStateFPGA2 = byteBuffer.get(690);// FPGA2时钟状态
		map1.put("clockTestStateFPGA2", Byte.toUnsignedInt(clockTestStateFPGA2) + radarString);

		short clockTestStateFPGAD = byteBuffer.getShort(691);// FPGA2 AD量化值
		map1.put("clockTestStateFPGAD", Short.toUnsignedInt(clockTestStateFPGAD) + radarString);
		int num1464 = byteBuffer.getInt(693);// 1464PDW个数
		map1.put("num1464", Integer.toUnsignedString(num1464));
		int num1532 = byteBuffer.getInt(697);// 1532PDW个数
		map1.put("num1532", Integer.toUnsignedString(num1532));
		short num1464IFF = byteBuffer.getShort(701);// 1464IFF个数
		map1.put("num1464IFF", Short.toUnsignedInt(num1464IFF) + radarString);
		short num1532IFF = byteBuffer.getShort(703);// 1532 IFF个数
		map1.put("num1532IFF", Short.toUnsignedInt(num1532IFF) + radarString); // （18）
		// 检测板
		byte numDSP = byteBuffer.get(705);// DSP计数
		map1.put("numDSP", Byte.toUnsignedInt(numDSP) + "");
		short informationPacageDSP = byteBuffer.getShort(706);// DSP信息包序号
		map1.put("informationPacageDSP", Short.toUnsignedInt(informationPacageDSP) + radarString);
		short cardWorkingTemp = byteBuffer.getShort(708);// 卡板工作温度
		map1.put("cardWorkingTemp", getBoardTemp(cardWorkingTemp));
		int num1464DSP = byteBuffer.getInt(710);// DSP1464全脉冲个数
		map1.put("num1464DSP", Integer.toUnsignedString(num1464DSP));
		int num1532DSP = byteBuffer.getInt(714);// DSP1532全脉冲个数
		map1.put("num1532DSP", Integer.toUnsignedString(num1532DSP));
		int num1464EnemyAndUsDSP = byteBuffer.getInt(718);// DSP 1464敌我个数
		map1.put("num1464EnemyAndUsDSP", Integer.toUnsignedString(num1464EnemyAndUsDSP));
		int num1532EnemyAndUsDSP = byteBuffer.getInt(722);// DSP 1532敌我个数
		map1.put("num1532EnemyAndUsDSP", Integer.toUnsignedString(num1532EnemyAndUsDSP)); // （21）
		byteBuffer.position(726);

		// =======================
		/* 关键状态end */
		byte[] chooseMCU = new byte[6];// MCU加载选择
		byteBuffer.get(chooseMCU);
		byteBuffer.position(732);
		byte[] restructureState = new byte[6]; // 重构状态
		byteBuffer.get(restructureState);
		short restructureNum = byteBuffer.getShort(738); // 重构发送包数
		map1.put("restructureNum", String.valueOf(restructureNum));
		short restructureTotal = byteBuffer.getShort(740);// 重构发送统计
		map1.put("restructureTotal", String.valueOf(restructureTotal));
		byteBuffer.position(742);
		byte[] bytes12 = new byte[64]; // 备份
		byteBuffer.get(bytes12);

		int end = byteBuffer.getInt(806); // 包尾
		// 结尾
		int verify = byteBuffer.getInt(810); // 校验和
		int messageEnd = byteBuffer.getInt(814); // 报文尾
		map1.put("host", host);
		simpMessagingTemplate.convertAndSend("/uploadEnemyAndUsPackageMessage/send",
				new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
	}

	// 解析系统控制信息
	private SystemControlBroadcastCMD systemControlBroadcastCMDs(byte[] bytes) {
		SystemControlBroadcastCMD systemControlBroadcastCMD = new SystemControlBroadcastCMD();
		ByteBuffer byteBuffer = ByteBuffer.allocate(64);
		byteBuffer.put(bytes);
		short header = byteBuffer.getShort();
		systemControlBroadcastCMD.setMessagePackageNum(byteBuffer.getShort(3));
		byte[] bytes1 = new byte[8];
		byteBuffer.get(bytes1, 5, 8);
		String time = new String(bytes1);
		systemControlBroadcastCMD.setTimeCode(time);
		systemControlBroadcastCMD.setWorkWay(byteBuffer.get(13));
		systemControlBroadcastCMD.setBandwidthChoose(byteBuffer.get(14));
		systemControlBroadcastCMD.setWorkCycleNum(byteBuffer.get(15));
		systemControlBroadcastCMD.setWorkCycleLength(byteBuffer.get(16));
		systemControlBroadcastCMD.setCenterFrequency(byteBuffer.getShort(17));
		systemControlBroadcastCMD.setDirectionFindingAntennaChoose(byteBuffer.get(19));
		systemControlBroadcastCMD.setScoutAntennaChoose(byteBuffer.get(20));
		systemControlBroadcastCMD.setPulseScreenMinimumFrequency(byteBuffer.get(27));
		systemControlBroadcastCMD.setPulseScreenMaximumFrequency(byteBuffer.getShort(29));
		systemControlBroadcastCMD.setPulseScreenMinimumRange(byteBuffer.get(31));
		systemControlBroadcastCMD.setPulseScreenMaximumRange(byteBuffer.get(32));
		systemControlBroadcastCMD.setPulseScreenMinimumPulseWidth(byteBuffer.getShort(33));
		systemControlBroadcastCMD.setPulseScreenMaximumPulseWidth(byteBuffer.getShort(35));
		byte[] bytes2 = new byte[16];
		byteBuffer.get(bytes2, 37, 16);
		systemControlBroadcastCMD.setRouteShield(bytes2);
		systemControlBroadcastCMD.setWithinThePulseGuidanceSwitch(byteBuffer.get(53));
		systemControlBroadcastCMD.setWithinThePulseGuidance(byteBuffer.get(54));
		systemControlBroadcastCMD.setUploadFullPulseNum(byteBuffer.getShort(55));
		byte[] bytes3 = new byte[2];
		byteBuffer.get(bytes3, 57, 2);
		StringBuilder stringBuilder = new StringBuilder();
		for (byte b : bytes3) {
			String tString = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
			stringBuilder.append(tString);
		}
		systemControlBroadcastCMD.setExtensionControl(stringBuilder.toString());
		String tString = Integer.toBinaryString((byteBuffer.get(59) & 0xFF) + 0x100).substring(1);
		systemControlBroadcastCMD.setEquipmentSerialNum(tString);
		systemControlBroadcastCMD.setDetectionThresholdAdjustment(byteBuffer.get(60));
		return systemControlBroadcastCMD;
	}

	// 查询该IP是否存在
	private boolean hasHostIP(String host) {
		Optional<AllHost> allHost = hostRepository.findByHost(host);
		return allHost.isPresent();
	}

	public List<AllHost> findAll() {
		return hostRepository.findAll();
	}

	// 解析mac
	private static String getMac(short s, int c) {
		String g;
		String d = Integer.toHexString(s);
		// 当short s为负数时 如：-10000 转换成16进制后为 FFFFD8F0 因为前4位没用，所以需要截取后4位。正数时则不需要
		if (s < 0) {
			g = d.substring(4);
			// 如果 s为0时，需要补齐零
		} else if (s == 0) {
			g = d + "000";
		} else {
			g = d;
		}

		String f = Integer.toHexString(c);
		if (c == 0) {
			f = f + "0000000";
		}
		String h = f + g;
		// 需要转换成大写形式显示给前端方便显示
		StringBuilder stringBuilder = new StringBuilder(h.toUpperCase());
		// 补上冒号 直接传给前端
		stringBuilder.insert(2, " - ");
		stringBuilder.insert(7, " - ");
		stringBuilder.insert(12, " - ");
		stringBuilder.insert(17, " - ");
		stringBuilder.insert(22, " - ");
		return stringBuilder.toString();
	}

	// 解析IP地址
	private static String getIP(byte[] bytes) {
		StringBuilder stringBuilder = new StringBuilder();
		// 为什么要从大到小 循环
		// 因为 192.168.31.88 这样类似的ip byte[3] 是192 所以要先从大到小循环
		for (int i = bytes.length - 1; i >= 0; i--) {
			/**
			 *
			 * 如果当前值小于0，即为负数 那么需要将当前值转换成16进制，再转成10进制 解释： 为什么会为负数？ 答：
			 * 因为当传过来的数大于127时，那么转成byte时会转换成负数 如168 --> 会变成 -88
			 * 这个时候就需要将-88先转换成16进制，再转换成String类型 转回168
			 */
			if (bytes[i] < 0) {
				/**
				 * 因为由后端拼接IP所以 当遍历到最后一个字节时 不需要再加小数点 所以要做判断
				 */
				if (i == 0) {
					stringBuilder.append(new BigInteger(Integer.toHexString(bytes[i]).substring(6), 16).toString());
				} else {
					stringBuilder.append(new BigInteger(Integer.toHexString(bytes[i]).substring(6), 16).toString())
							.append(".");
				}
			} else {
				if (i == 0) {
					stringBuilder.append(bytes[i]);
				} else {
					// 当为正数时 不需要判断 直接在末尾加小数点
					stringBuilder.append(bytes[i]).append(".");
				}
			}
		}
		return stringBuilder.toString();
	}

	// ip解析
	// 获取到10进制的ip转换成127.0.0.1形式
	public static String getToIp(int ip) {

		StringBuffer sb = new StringBuffer("");
		sb.append(ip & 0xFF);
		sb.append(".");
		sb.append(ip >> 8 & 0xFF);
		sb.append(".");
		sb.append(ip >> 16 & 0xFF);
		sb.append(".");
		sb.append(ip >> 24 & 0xFF);
		return sb.toString();
	}

	// 二进制转换成10进制
	public static int BinaryToDecimal(int binaryNumber) {
		int decimal = 0;
		int p = 0;
		while (true) {
			if (binaryNumber == 0) {
				break;
			} else {
				int temp = binaryNumber % 10;
				decimal += temp * Math.pow(2, p);
				binaryNumber = binaryNumber / 10;
				p++;
			}
		}
		return decimal;
	}

	// 解析主控DSP版本号
	private static Integer getDsp(byte dsp) {
		String dsString = Integer.toBinaryString((dsp & 0xFF) + 0x100).substring(1);
		BigInteger bigInteger = new BigInteger(dsString, 2);
		Integer ds = Integer.parseInt(bigInteger.toString());
		return ds;
	}

	// 截取单个bit（short类型）
	public static StringBuilder getShort(short shorts) {
		String radar = Integer.toBinaryString(shorts);
		StringBuilder stringBuilder4 = new StringBuilder();
		int a1 = 8 - radar.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			// stringBuilder3.append(b);
			radar = bString1 + radar;
		}
		StringBuilder stringBuilder2 = new StringBuilder(radar);
		stringBuilder2.reverse();
		// 每两位转10进制数值int输出
		for (int i = 2; i < 8; i += 1) {
			String rstr = stringBuilder2.substring(i, i + 1);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			stringBuilder4.append(ss);
		}
		return stringBuilder4;
	}

	// 获取单个bit值
	public static StringBuilder getByte(byte a) {
		String po = Integer.toBinaryString((a & 0xFF) + 0x100).substring(1);
		StringBuilder stringBuilderdsp = new StringBuilder();
		int a2 = 8 - po.length();
		String bString2 = "0";
		for (int i = 0; i < a2; i++) {
			po = bString2 + po;
		}
		StringBuilder stringBuilder = new StringBuilder(po);
		stringBuilder.reverse();
		for (int i = 0; i < 8; i += 1) {
			String rstr = stringBuilder.substring(i, i + 1);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			stringBuilderdsp.append(ss);
		}
		return stringBuilderdsp;
	}

	// 指令状态解析
	public String getCommondState(byte b) {
		String binaryString = getBinaryString(b);
		String temp = new StringBuilder(binaryString).reverse().substring(0, 4) + binaryString.substring(0, 4);
		String result = "";
		result = result + temp.substring(0, 1);
		result = result + temp.substring(1, 2);
		result = result + temp.substring(2, 3);
		result = result + temp.substring(3, 4);
		result = result + Integer.parseUnsignedInt(temp.substring(4, 8), 2);
		return result;
	}

	// 截取两个bit值解析（8字节）
	// 获取两个bit值
	public static StringBuilder getDouble(byte doubles) {
		String masterControl1 = Integer.toBinaryString(doubles);
		StringBuilder stringBuilder4 = new StringBuilder();
		int a1 = 8 - masterControl1.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			// stringBuilder3.append(b);
			masterControl1 = bString1 + masterControl1;
		}
		StringBuilder stringBuilder2 = new StringBuilder(masterControl1);
		stringBuilder2.reverse();
		// 每两位转10进制数值int输出
		for (int i = 2; i < 8; i += 2) {
			String rstr = stringBuilder2.substring(i, i + 2);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			stringBuilder4.append(ss);
		}
		return stringBuilder4;
	}

	// 解析分机状态
	public static String getFenjiState(short control) {
		String resultString = "";
		String binaryString = Integer.toBinaryString(Short.toUnsignedInt(control));
		int leftNum = 16 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		resultString = resultString + binaryString.substring(binaryString.length() - 1);
		resultString = resultString + Integer.parseInt(binaryString.substring(3, 5), 2);
		return resultString;
	}

	// 解析工作状态分机控制
	public static String getFenjiControl(short control) {
		String resultString = "";
		String binaryString = Integer.toBinaryString(Short.toUnsignedInt(control));
		int leftNum = 16 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		resultString = resultString + binaryString.substring(15);
		resultString = resultString + binaryString.substring(14, 15);
		return resultString;
	}

	// 解析卡板工作温度
	public static String getCardTemp(short cardTemp) {
		String temp = Integer.toBinaryString((cardTemp & 0xFF) + 0x100).substring(1);
		String worktemp = "";
		int a1 = 32 - temp.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			// stringBuilder3.append(b);
			temp = bString1 + temp;
		}
		// 每两位转10进制数值int输出
		for (int i = 0; i < 8; i += 4) {
			String rstr = temp.substring(i, i + 4);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			worktemp = worktemp + "." + ss;
		}
		return worktemp;
	}

	// 序号解析(32位)
	public static int getNum(short num) {
		String masterControl1 = Integer.toBinaryString((num & 0xFF) + 0x100).substring(1);
		int a1 = 16 - masterControl1.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			masterControl1 = bString1 + masterControl1;
		}
		StringBuilder stringBuilder2 = new StringBuilder(masterControl1);
		stringBuilder2.reverse();
		// 每两位转10进制数值int输出
		String rstr = stringBuilder2.substring(0);
		BigInteger bigInteger = new BigInteger(rstr, 2);
		Integer ss = Integer.parseInt(bigInteger.toString());
		return ss;
	}

	// 分机工作状态
	public static StringBuilder getWorkStates(long num) {
		String workStates = Integer.toBinaryString((int) ((num & 0xFF) + 0x100)).substring(1);
		StringBuilder stringBuilder4 = new StringBuilder();
		int a1 = 64 - workStates.length();
		String bString1 = "0";
		for (int i = 0; i < a1; i++) {
			// stringBuilder3.append(b);
			workStates = bString1 + workStates;
		}
		StringBuilder stringBuilder2 = new StringBuilder(workStates);
		stringBuilder2.reverse();
		// 每两位转10进制数值int输出
		for (int i = 0; i < 64; i += 1) {
			String rstr = stringBuilder2.substring(i, i + 1);
			BigInteger bigInteger = new BigInteger(rstr, 2);
			Integer ss = Integer.parseInt(bigInteger.toString());
			stringBuilder4.append(ss);
		}
		return stringBuilder4;
	}

	// 解析雷达分机工作状态
	private String radarWorkState(long num) {
		String resultState = "";
		String state = Long.toBinaryString(num);
		int leftNum = 64 - state.length();
		for (int i = 0; i < leftNum; i++) {
			state = "0" + state;
		}
		resultState = resultState + state.substring(63, 64);// 0
		resultState = resultState + state.substring(62, 63);// 1
		resultState = resultState + state.substring(61, 62);// 2
		resultState = resultState + state.substring(60, 61);// 3
		resultState = resultState + state.substring(59, 60);// 4
		resultState = resultState + state.substring(58, 59);// 5
		resultState = resultState + state.substring(57, 58);// 6
		resultState = resultState + state.substring(56, 57);// 7
		resultState = resultState + state.substring(55, 56);// 8
		resultState = resultState + state.substring(54, 55);// 9

		resultState = resultState + state.substring(47, 48);// 16
		resultState = resultState + state.substring(46, 47);// 17
		resultState = resultState + state.substring(45, 46);// 18
		resultState = resultState + state.substring(44, 45);// 19
		resultState = resultState + state.substring(43, 44);// 20
		resultState = resultState + state.substring(42, 43);// 21
		resultState = resultState + state.substring(41, 42);// 22
		resultState = resultState + state.substring(40, 41);// 23
		resultState = resultState + state.substring(39, 40);// 24
		resultState = resultState + state.substring(38, 39);// 25

		resultState = resultState + state.substring(31, 32);// 32
		resultState = resultState + state.substring(30, 31);// 33
		resultState = resultState + state.substring(29, 30);// 34
		resultState = resultState + state.substring(28, 29);// 35

		resultState = resultState + state.substring(26, 27);// 37
		resultState = resultState + state.substring(25, 26);// 38
		resultState = resultState + state.substring(24, 25);// 39
		resultState = resultState + state.substring(23, 24);// 40

		resultState = resultState + state.substring(20, 21);// 43
		resultState = resultState + state.substring(19, 20);// 44
		resultState = resultState + state.substring(18, 19);// 45
		resultState = resultState + state.substring(17, 18);// 46
		resultState = resultState + state.substring(16, 17);// 47
		resultState = resultState + state.substring(15, 16);// 48
		resultState = resultState + state.substring(14, 15);// 49
		resultState = resultState + state.substring(13, 14);// 50

		resultState = resultState + state.substring(9, 10);// 54
		resultState = resultState + state.substring(8, 9);// 55
		resultState = resultState + state.substring(7, 8);// 56
		resultState = resultState + state.substring(6, 7);// 57
		return resultState;
	}

	// 时间码解析
	private String longToBinaryString(long longNum) {
		String binaryString = Long.toBinaryString(longNum);
		int leftNum = 64 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}

	private String errorCheck(byte value) {
		String result = "";
		String binaryString = Integer.toBinaryString(Byte.toUnsignedInt(value));
		int leftNum = 8 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		result = result + binaryString.substring(7);
		result = result + binaryString.substring(6, 7);
		return result;
	}

	private String getBinaryString(byte b) {
		String binaryString = Integer.toBinaryString(Byte.toUnsignedInt(b));
		int leftNum = 8 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}

	private String getBinaryString(short s) {
		String binaryString = Integer.toBinaryString(s);
		int leftNum = 16 - binaryString.length();
		for (int i = 0; i < leftNum; i++) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}

	private String getLockingInfo(byte b) {
		String binaryString = getBinaryString(b);
		String result = "";
		result = result + binaryString.substring(7);
		result = result + binaryString.substring(5, 6);
		result = result + binaryString.substring(2, 3);// 自检源锁定指示
		return result;
	}

	private String getSystemWorkState(byte b) {
		String binaryString = getBinaryString(b);
		String resultstring = "";
		resultstring = resultstring + binaryString.substring(7, 8);
		resultstring = resultstring + binaryString.substring(6, 7);
		return resultstring;
	}

	// 主控
	private String getZhuKongJiShuNum1(byte b) {
		String binaryString = getBinaryString(b);
		String resultstring = "";
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(6, 8), 2);
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(4, 6), 2);
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(2, 4), 2);
		return resultstring;
	}

	// 检测
	private String getZhuKongJiShuNum2(byte b) {
		String binaryString = getBinaryString(b);
		String resultstring = "";
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(6, 8), 2);
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(4, 6), 2);
		resultstring = resultstring + Integer.parseUnsignedInt(binaryString.substring(2, 4), 2);
		return resultstring;
	}

	// 采样时钟解析
	private String getSamplingClock(byte b) {
		String binaryString = getBinaryString(b);
		String resultstring = "";
		resultstring = resultstring + binaryString.substring(7, 8);
		resultstring = resultstring + binaryString.substring(6, 7);
		return resultstring;
	}

	private String getBoardTemp(short s) {
		String binaryString = getBinaryString(s);
		String resultstring = "";
		resultstring = resultstring + Short.parseShort(binaryString.substring(8, 16), 2) + ".";
		resultstring = resultstring + Short.parseShort(binaryString.substring(0, 8), 2);
		return resultstring;
	}

	private String getCardState(byte b) {
		String binaryString = getBinaryString(b);
		String resultstring = "";
		resultstring = resultstring + binaryString.substring(7, 8);
		resultstring = resultstring + binaryString.substring(6, 7);
		resultstring = resultstring + binaryString.substring(5, 6);
		resultstring = resultstring + binaryString.substring(4, 5);
		resultstring = resultstring + binaryString.substring(3, 4);
		resultstring = resultstring + binaryString.substring(2, 3);
		resultstring = resultstring + binaryString.substring(1, 2);
		resultstring = resultstring + binaryString.substring(0, 1);
		return resultstring;
	}

	private void saveErrorLog(String host, String state) {
		String deviceState = globalMap.get(host);
		// 第一次初始化状态
		if (deviceState == null || deviceState == "") {
			globalMap.put(host, state);
			return;
		}
		// 和取回的状态比较
		if (!deviceState.equals(state)) {
			for (int i = 0; i < state.length(); i++) {
				char lastState = deviceState.charAt(i);
				char nowState = state.charAt(i);
				if (lastState != nowState) {
					if (lastState == '0') {
						SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
						sysErrorLogEntity.setCreateTime(new Date());
						sysErrorLogEntity.setErrorType("组件故障");
						sysErrorLogEntity.setErrorMsg(state);
						sysErrorLogEntity.setHost(host);
						sysErrorLogService.saveError(sysErrorLogEntity);
						break;
					}
				}
			}
		}
		globalMap.put(host, state);
	}
}
