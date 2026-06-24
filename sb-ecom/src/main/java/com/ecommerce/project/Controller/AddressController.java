package com.ecommerce.project.Controller;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {
    AuthUtil authUtil;
    AddressService addressService;
    public AddressController(AddressService addressService,AuthUtil authUtil){
        this.addressService = addressService;
        this.authUtil = authUtil;
    }
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO){
        User user = authUtil.loggedInUser();
        AddressDTO addressDTO1 = addressService.createAddress(addressDTO,user);
        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddress(){
        List<AddressDTO> addressList = addressService.getAddresses();
        return new ResponseEntity<>(addressList,HttpStatus.OK);

    }
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){

        AddressDTO addressDTO1 = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }
    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getAddressByUser(){
        User user = authUtil.loggedInUser();
        List<AddressDTO> addressList = addressService.getAddressByUser(user);
        return new ResponseEntity<>(addressList,HttpStatus.OK);
    }
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@RequestBody AddressDTO addressDTO,
                                                    @PathVariable Long addressId){

        AddressDTO addressDTO1 = addressService.updateAddress(addressDTO,addressId);
        return  new ResponseEntity<>(addressDTO1,HttpStatus.OK);
    }
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String>  deleteAddress(@PathVariable Long addressId){
        String message  =  addressService.deleteAddress(addressId);
        return new ResponseEntity<>(message,HttpStatus.OK);
    }

}
