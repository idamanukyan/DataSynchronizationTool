package com.example.datasynchronizationtool.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    @Column(name = "is_bi_directional")
    private boolean isBiDirectional;

    @ManyToOne
    @JoinColumn(name = "sync_configuration_id")
    private SyncConfiguration syncConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public boolean isBiDirectional() {
        return isBiDirectional;
    }

    public void setBiDirectional(boolean biDirectional) {
        isBiDirectional = biDirectional;
    }

    public SyncConfiguration getSyncConfiguration() {
        return syncConfiguration;
    }

    public void setSyncConfiguration(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }
}
