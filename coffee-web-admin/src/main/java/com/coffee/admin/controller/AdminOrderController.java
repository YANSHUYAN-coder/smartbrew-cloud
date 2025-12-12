package com.coffee.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@AllArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('oms:order:list')")
    public Result<Page<OmsOrder>> list(PageParam pageParam,
                                       @RequestParam(required = false) Integer status) {
        return Result.success(orderService.getAllList(pageParam, status));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('oms:order:detail')")
    public Result<OrderVO> detail(@PathVariable("id") Long id) {
        return Result.success(orderService.getDetail(id));
    }

    @PostMapping("/updateStatus")
    @PreAuthorize("hasAuthority('oms:order:status')")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        boolean success = orderService.updateStatus(params);
        return success ? Result.success("更新成功") : Result.failed("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('oms:order:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = orderService.removeById(id);
         return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}
