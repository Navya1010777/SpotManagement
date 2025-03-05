package com.qpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_info")
public class VehicleInfo {

    @Id
    private int vehicleId;

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
}
