package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.AllHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备连接
 * @author yaojiahao
 * @data 2019/4/29 19:17
 */
@Repository
public interface HostRepository extends JpaRepository<AllHost,String> {
     Optional<AllHost> findByHost(String host);

     // 查询当前num的最大值；
     @Query(value = "select a from AllHost a where a.num=(select max(a.num) from a)")
     AllHost findMaxByNum();

     List<AllHost> findByHostNotLike(String hostName);
}
