package com.coffee.app.controller;

import com.coffee.common.context.UserContext;
import com.coffee.common.result.Result;
import com.coffee.common.util.JwtUtil;
import com.coffee.system.domain.UmsMemberReceiveAddress;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端收货地址管理接口
 */
@RestController
@RequestMapping("/app/address")
public class AppAddressController {

    @Autowired
    private UmsMemberReceiveAddressService addressService;

    // 1. 获取我的收货地址列表
    @GetMapping("/list")
    public Result<List<UmsMemberReceiveAddress>> list() {
        return Result.success(addressService.listCurrent());
    }

    // 2. 添加收货地址
    @PostMapping("/add")
    public Result<String> add(@RequestBody UmsMemberReceiveAddress address) {
        return addressService.addAddress(address);
    }

    // 3. 修改收货地址
    @PostMapping("/update")
    public Result<String> update(@RequestBody UmsMemberReceiveAddress address) {
        return addressService.updateAddress(address);
    }

    // 4. 删除收货地址
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }


}