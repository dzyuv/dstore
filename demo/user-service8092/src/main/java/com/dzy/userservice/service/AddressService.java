package com.dzy.userservice.service;

import com.dzy.userservice.dto.AddressRequest;
import com.dzy.userservice.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> listByUser(Long userId);
    Address getDefault(Long userId);
    Address getByIdAndUser(Long addressId, Long userId);
    Address addAddress(Long userId, AddressRequest request);
    Address updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}
