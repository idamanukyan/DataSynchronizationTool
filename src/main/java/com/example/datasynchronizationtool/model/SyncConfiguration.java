package com.example.datasynchronizationtool.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sync_configuration")
public class SyncConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "source_system")
    private String sourceSystem;

    @Column(name = "target_system")
    private String targetSystem;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "syncConfiguration", cascade = CascadeType.ALL)
    private List<FieldMapping> fieldMappings;

    @OneToOne(mappedBy = "syncConfiguration", cascade = CascadeType.ALL)
    private SyncSchedule syncSchedule;

}
