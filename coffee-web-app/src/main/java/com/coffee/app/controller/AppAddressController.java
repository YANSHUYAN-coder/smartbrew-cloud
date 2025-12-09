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
    @Autowired
    private JwtUtil jwtUtil;

    // 1. 获取我的收货地址列表
    @GetMapping("/list")
    public Result<List<UmsMemberReceiveAddress>> list() {
        Long userId = UserContext.getUserId();
        List<UmsMemberReceiveAddress> list = addressService.listCurrent(userId);
        return Result.success(list);
    }

    // 2. 添加收货地址
    @PostMapping("/add")
    public Result<String> add(@RequestBody UmsMemberReceiveAddress address) {
        Long userId = UserContext.getUserId();
        address.setMemberId(userId);
        boolean success = addressService.add(address);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }

    // 3. 修改收货地址
    @PostMapping("/update")
    public Result<String> update(@RequestBody UmsMemberReceiveAddress address) {
        Long userId = UserContext.getUserId();
        // 安全检查：只能修改自己的地址
        UmsMemberReceiveAddress exist = addressService.getById(address.getId());
        if (exist == null || !exist.getMemberId().equals(userId)) {
            return Result.failed("非法操作");
        }
        
        address.setMemberId(userId); // 确保ID不被篡改
        boolean success = addressService.updateAddress(address);
        return success ? Result.success("修改成功") : Result.failed("修改失败");
    }

    // 4. 删除收货地址
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        UmsMemberReceiveAddress exist = addressService.getById(id);
        if (exist == null || !exist.getMemberId().equals(userId)) {
            return Result.failed("非法操作");
        }
        boolean success = addressService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }


}