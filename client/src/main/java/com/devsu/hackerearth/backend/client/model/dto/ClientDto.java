package com.devsu.hackerearth.backend.client.model.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

	private Long id;
	@NotBlank
	private String dni;
	@NotBlank
	private String name;
	private String password;
	private String gender;
	@Min(0)
	private int age;
	private String address;
	private String phone;
	private boolean isActive;
}
