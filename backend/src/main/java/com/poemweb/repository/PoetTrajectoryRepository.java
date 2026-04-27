package com.poemweb.repository;

import com.poemweb.entity.PoetTrajectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoetTrajectoryRepository extends JpaRepository<PoetTrajectory, Long> {
    List<PoetTrajectory> findByPoetNameOrderByYearAsc(String poetName);
    
    List<PoetTrajectory> findAllByOrderByPoetNameAscYearAsc();
    
    @Query("SELECT DISTINCT p.poetName FROM PoetTrajectory p")
    List<String> findDistinctPoetNames();
    
    void deleteByPoetName(String poetName);
}
