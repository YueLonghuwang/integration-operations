package com.rengu.project.integrationoperations.service;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.util.ApplicationMessages;

@Service
public class HostService {

	private final HostRepository hostRepository;

	@Autowired
	public HostService(HostRepository hostRepository) {
		this.hostRepository = hostRepository;
	}

	@CacheEvict(value = "Host_Cache", allEntries = true)
	public AllHost updateIDAndIP(String hostId, AllHost allHost) {
		AllHost allHost2 = getById(hostId);
		if (!StringUtils.isEmpty(allHost2.getHost()) && !allHost2.getHost().equals(allHost.getHost())) {
			allHost2.setHost(allHost.getHost());
		}
		if (!StringUtils.isEmpty(allHost2.getStationID()) && allHost2.getStationID() != allHost.getStationID()) {
			allHost2.setStationID(allHost.getStationID());
		}
		return hostRepository.save(allHost2);
	}

	// 根据id检查设备
	@Cacheable(value = "Host_Cache", key = "#hostID")
	public AllHost getById(String hostID) {
		if (!hasStationById(hostID)) {
			throw new RuntimeCryptoException(ApplicationMessages.STATION_ID_NOT_FOUND);
		}
		return hostRepository.findById(hostID).get();
	}

	// 根据id判断设备是否存在
	@Cacheable(value = "Host_Cache", key = "#hostID")
	public boolean hasStationById(String hostID) {
		if (StringUtils.isEmpty(hostID)) {
			return false;
		}
		return hostRepository.existsById(hostID);
	}
}
