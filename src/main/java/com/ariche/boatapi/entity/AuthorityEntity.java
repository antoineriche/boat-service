package com.ariche.boatapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "_authority", schema = "public")
public class AuthorityEntity implements Serializable {

    @Id
    @NotBlank(message = "{authority.validation.error.name.blank}")
    @Size(max = 50, message = "{authority.validation.error.name.invalid.length}")
    @Column(length = 50)
    private String name;

}
