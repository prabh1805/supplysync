package com.supplysync.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private String id;
    private String name;
    private String address;
    private String email;
    private String phone;
    private BigDecimal rating;
    private Boolean active;
}
