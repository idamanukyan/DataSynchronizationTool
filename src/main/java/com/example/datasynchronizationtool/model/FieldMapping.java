package com.example.datasynchronizationtool.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "field_mapping")
public class FieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_field")
    private String sourceField;

    @Column(name = "target_field")
    private String targetField;

    @Column(name = "transformation")
    private String transformation;

    @ManyToOne
    @JoinColumn(name = "sync_configuration_id")
    private SyncConfiguration syncConfiguration;
}
