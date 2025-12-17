package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端收货地址管理接口
 */
@RestController
@RequestMapping("/app/address")
@Tag(name = "C端-收货地址接口", description = "提供收货地址的新增、修改、删除和列表查询能力")
public class AppAddressController {

    @Autowired
    private UmsMemberReceiveAddressService addressService;

    // 1. 获取我的收货地址列表
    @GetMapping("/list")
    @Operation(summary = "获取我的收货地址列表", description = "返回当前登录用户的全部收货地址，按最近更新时间排序")
    public Result<List<UmsMemberReceiveAddress>> list() {
        return Result.success(addressService.listCurrent());
    }

    // 2. 添加收货地址
    @PostMapping("/add")
    @Operation(summary = "新增收货地址", description = "为当前登录用户新增一条收货地址，可设置为默认地址")
    public Result<String> add(@RequestBody UmsMemberReceiveAddress address) {
        return addressService.addAddress(address);
    }

    // 3. 修改收货地址
    @PostMapping("/update")
    @Operation(summary = "修改收货地址", description = "根据地址ID修改对应的收货地址信息")
    public Result<String> update(@RequestBody UmsMemberReceiveAddress address) {
        return addressService.updateAddress(address);
    }

    // 4. 删除收货地址
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除收货地址", description = "根据地址ID删除对应的收货地址记录")
    public Result<String> delete(@PathVariable("id") Long id) {
        return addressService.deleteAddress(id);
    }


}