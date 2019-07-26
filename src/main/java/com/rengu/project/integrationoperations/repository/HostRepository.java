package com.rengu.project.integrationoperations.repository;

import com.rengu.project.integrationoperations.entity.AllHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author yaojiahao
 * @data 2019/4/29 19:17
 */
@Repository
public interface HostRepository extends JpaRepository<AllHost,String> {  //JpaRepository是实现Spring Data JPA技术访问数据库的关键接口
     Optional<AllHost> findByHost(String host);                          //HostRepository 主机存储库

     // 查询当前num的最大值；
     @Query(value = "select a from AllHost a where a.num=(select max(a.num) from a)")
     AllHost findMaxByNum();
}
