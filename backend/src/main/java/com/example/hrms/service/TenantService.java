package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.entity.Tenant;
import com.example.hrms.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户服务
 */
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;

    /**
     * 分页查询租户
     */
    public Page<Tenant> page(long current, long size, String keyword) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Tenant::getTenantName, keyword)
                    .or().like(Tenant::getTenantCode, keyword);
        }
        wrapper.orderByDesc(Tenant::getCreateTime);
        return tenantMapper.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 获取所有租户
     */
    public List<Tenant> listAll() {
        return tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getStatus, "ACTIVE")
                .orderByAsc(Tenant::getId));
    }

    /**
     * 根据ID获取租户
     */
    public Tenant getById(Long id) {
        return tenantMapper.selectById(id);
    }

    /**
     * 新增租户
     */
    public Tenant create(Tenant tenant) {
        // 检查编码重复
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getTenantCode, tenant.getTenantCode()));
        if (count != null && count > 0) {
            throw new BizException("租户编码已存在");
        }
        if (tenant.getStatus() == null) {
            tenant.setStatus("ACTIVE");
        }
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * 更新租户
     */
    public Tenant update(Tenant tenant) {
        Tenant existing = tenantMapper.selectById(tenant.getId());
        if (existing == null) {
            throw new BizException("租户不存在");
        }
        // 检查编码重复（排除自己）
        Long count = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getTenantCode, tenant.getTenantCode())
                .ne(Tenant::getId, tenant.getId()));
        if (count != null && count > 0) {
            throw new BizException("租户编码已存在");
        }
        tenantMapper.updateById(tenant);
        return tenant;
    }

    /**
     * 删除租户
     */
    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }
}
