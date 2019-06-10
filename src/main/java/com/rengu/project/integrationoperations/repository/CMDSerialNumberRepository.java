package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.CMDSerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author yaojiahao
 * @data 2019/4/30 13:23
 */
@Repository
public interface CMDSerialNumberRepository extends JpaRepository<CMDSerialNumber, String> {

}
