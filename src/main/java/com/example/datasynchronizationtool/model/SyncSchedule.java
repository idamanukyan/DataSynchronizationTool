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
@Table(name = "sync_schedule")
public class SyncSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cron_expression")
    private String cronExpression;

    @OneToOne
    @JoinColumn(name = "sync_configuration_id", unique = true)
    private SyncConfiguration syncConfiguration;
}
