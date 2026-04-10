package com.supplysync.supplier.service;

import com.supplysync.supplier.dto.SupplierRequest;
import com.supplysync.supplier.dto.SupplierResponse;
import com.supplysync.supplier.entity.Supplier;
import com.supplysync.supplier.exception.InvalidRequestException;
import com.supplysync.supplier.exception.SupplierNotFoundException;
import com.supplysync.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierResponse createSupplier(SupplierRequest supplierRequest) {
        if(supplierRepository.findByEmail(supplierRequest.getEmail()).isPresent()) {
            throw new InvalidRequestException("Email already in use");
        }
        if(supplierRequest.getName().isEmpty()) {
            throw new InvalidRequestException("Name cannot be empty");
        }
        if(supplierRequest.getPhone().isEmpty()) {
            throw new InvalidRequestException("Phone number cannot be empty");
        }
        Supplier supplier = Supplier.builder()
                .email(supplierRequest.getEmail())
                .name(supplierRequest.getName())
                .phone(supplierRequest.getPhone())
                .rating(BigDecimal.valueOf(1.0))
                .address(supplierRequest.getAddress())
                .active(true)
                .build();
        supplierRepository.save(supplier);
        return mapToResponse(supplier);
    }
    public SupplierResponse getById(String id) {
        UUID uuid = UUID.fromString(id);
        Supplier supplier = supplierRepository.findById(uuid)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier with id '" + id + "' not found"));
        return mapToResponse(supplier);
    }
    public Page<SupplierResponse> getAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return supplierRepository.findAll(PageRequest.of(page, size, sort))
                .map(this::mapToResponse);
    }

    public SupplierResponse updateSupplier(String id, SupplierRequest supplierRequest) {
        UUID uuid = UUID.fromString(id);
        Supplier supplier = supplierRepository.findById(uuid)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found"));
        supplier.setName(supplierRequest.getName());
        supplier.setPhone(supplierRequest.getPhone());
        supplier.setAddress(supplierRequest.getAddress());

        supplierRepository.save(supplier);
        return mapToResponse(supplier);
    }

    public void deleteSupplier(String id) {
        UUID uuid = UUID.fromString(id);
        Supplier supplier = supplierRepository.findById(uuid)
                        .orElseThrow(() -> new SupplierNotFoundException("Supplier not found"));
        supplierRepository.delete(supplier);
    }

    public Page<SupplierResponse> searchByName(int page, int size, String sortBy,
                                               String direction, String name) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return supplierRepository
                .findByNameContainingIgnoreCase(name, pageRequest)
                .map(this::mapToResponse);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(String.valueOf(supplier.getId()))
                .email(supplier.getEmail())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .rating(supplier.getRating())
                .active(supplier.isActive())
                .build();
    }
}
