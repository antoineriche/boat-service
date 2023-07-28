package com.ariche.boatapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "BoatEntity")
@Table(name = "boats", schema = "public")
public class BoatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "boat_sequence")
    @SequenceGenerator(name = "boat_sequence", sequenceName = "boat_sequence", allocationSize = 1, initialValue = 1)
    private Long id;

    @NotBlank(message = "{boat.validation.error.name.blank}")
    @Size(min = 2, max = 100, message = "{boat.validation.error.name.invalid.length}")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 2_000, message = "{boat.validation.error.description.too.large}")
    @Column(name = "description", nullable = true, length = 2000)
    private String description;

    @Column(name = "img_name", nullable = true, length = 255)
    private String imgName;

}
