package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.AllHost;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yaojiahao
 * @data 2019/4/29 19:17
 */
public interface HostRepository extends JpaRepository<AllHost,String> {

}
