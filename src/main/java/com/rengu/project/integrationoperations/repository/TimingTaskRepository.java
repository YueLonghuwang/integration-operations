package com.rengu.project.integrationoperations.repository;


import com.rengu.project.integrationoperations.entity.TimingTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimingTaskRepository extends JpaRepository<TimingTasks,String> {

}
