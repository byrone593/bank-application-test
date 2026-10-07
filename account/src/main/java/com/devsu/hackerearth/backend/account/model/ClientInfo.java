package com.devsu.hackerearth.backend.account.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    @Id
    private Long id; // el id lo asigna el servicio Client
    private String name;
    private boolean active;
}