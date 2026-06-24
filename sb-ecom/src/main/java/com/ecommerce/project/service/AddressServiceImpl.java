package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositery.AddressRepository;
import com.ecommerce.project.repositery.UserRepository;
import jakarta.persistence.Id;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    AddressRepository addressRepository;
    UserRepository userRepository;
    ModelMapper modelMapper;
    public AddressServiceImpl(AddressRepository addressRepository,UserRepository userRepository,ModelMapper modelMapper){
        this.addressRepository=addressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public AddressDTO createAddress(AddressDTO addressDTO,User user) {
        List<Address> addressList = user.getAddresses();
        Address address = modelMapper.map(addressDTO,Address.class);
        if(addressList.contains(address)){
            throw new APIException("Address with Address Id " + address.getAddressId() + " already exist.");
        }
        addressList.add(address);
        user.setAddresses(addressList);
        address.setUser(user);
        Address address1 =addressRepository.save(address);
        return modelMapper.map(address1,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(address ->
                modelMapper.map(address,AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() ->
                new ResourceNotFoundException("Address","Addressid",addressId));
        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressByUser(User user) {
        List<Address> addresses = user.getAddresses();
        List<AddressDTO> addressDTOS = addresses.stream().map(address ->
                modelMapper.map(address,AddressDTO.class)).toList();
        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {

        Address address = addressRepository.findById(addressId).orElseThrow(() ->
                new ResourceNotFoundException("Address","Addressid",addressId));
        address.setCity(addressDTO.getCity());
        address.setPincode(addressDTO.getPincode());
        address.setState(addressDTO.getState());
        address.setCity(addressDTO.getCity());
        address.setBuildingName(addressDTO.getBuildingName());
        address.setCountry(addressDTO.getCountry());
        Address updatedAddress = addressRepository.save(address);
        User user = address.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress,AddressDTO.class);

    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(() ->
                new ResourceNotFoundException("Address","Addressid",addressId));
        User user = address.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        userRepository.save(user);
        return "Address deleted successfully with addressId:" + addressId;
    }
}
