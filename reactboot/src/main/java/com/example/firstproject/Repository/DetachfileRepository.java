package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firstproject.Entity.detachfile;


public interface DetachfileRepository extends JpaRepository<detachfile,Long>{

	List<detachfile> findByPathContaining(String path);
}
