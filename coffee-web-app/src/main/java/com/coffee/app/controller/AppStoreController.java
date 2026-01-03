package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.OmsStore;
import com.coffee.system.service.OmsStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端-门店信息接口
 */
@Tag(name = "C端-门店信息接口")
@RestController
@RequestMapping("/app/store")
@RequiredArgsConstructor
public class AppStoreController {

    private final OmsStoreService storeService;

    @Operation(summary = "获取门店信息")
    @GetMapping("/info")
    public Result<OmsStore> getInfo(@RequestParam(required = false) Long storeId) {
        if (storeId != null) {
            return Result.success(storeService.getById(storeId));
        }
        return Result.success(storeService.getDefaultStore());
    }

    @Operation(summary = "获取所有门店列表")
    @GetMapping("/list")
    public Result<List<OmsStore>> list(@RequestParam(required = false) Double longitude, 
                                       @RequestParam(required = false) Double latitude) {
        return Result.success(storeService.listNearby(longitude, latitude));
    }
}

