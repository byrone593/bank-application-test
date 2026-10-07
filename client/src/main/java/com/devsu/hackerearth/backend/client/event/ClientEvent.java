package com.devsu.hackerearth.backend.client.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEvent {

    private Long clientId;
    private String name;
    private boolean active; // al eliminar el cliente se publica active=false

}
