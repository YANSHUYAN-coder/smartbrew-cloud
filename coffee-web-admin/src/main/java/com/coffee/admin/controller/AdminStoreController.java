package com.coffee.admin.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.OmsStore;
import com.coffee.system.service.OmsStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-门店管理接口
 */
@Tag(name = "管理端-门店管理接口")
@RestController
@RequestMapping("/admin/store")
@RequiredArgsConstructor
public class AdminStoreController {

    private final OmsStoreService storeService;

    @Operation(summary = "获取所有门店列表")
    @GetMapping("/list")
    public Result<List<OmsStore>> list() {
        return Result.success(storeService.list());
    }

    @Operation(summary = "新增门店")
    @PostMapping("/add")
    public Result<String> add(@RequestBody OmsStore store) {
        boolean success = storeService.save(store);
        return success ? Result.success("新增成功") : Result.failed("新增失败");
    }

    @Operation(summary = "获取门店信息")
    @GetMapping("/info")
    public Result<OmsStore> getInfo() {
        return Result.success(storeService.getDefaultStore());
    }

    @Operation(summary = "更新门店信息")
    @PostMapping("/update")
    public Result<String> update(@RequestBody OmsStore store) {
        boolean success = storeService.updateById(store);
        return success ? Result.success("更新成功") : Result.failed("更新失败");
    }

    @Operation(summary = "删除门店")
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = storeService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }

    @Operation(summary = "快速切换营业状态")
    @PostMapping("/status/open/{id}/{status}")
    public Result<String> updateOpenStatus(@PathVariable Long id, @PathVariable Integer status) {
        OmsStore store = storeService.getById(id);
        if (store == null) return Result.failed("未找到门店信息");
        store.setOpenStatus(status);
        storeService.updateById(store);
        return Result.success("状态已更新");
    }
}

