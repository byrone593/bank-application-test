package com.devsu.hackerearth.backend.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsu.hackerearth.backend.account.model.ClientInfo;

public interface ClientInfoRepository extends JpaRepository<ClientInfo, Long> {
    
}
