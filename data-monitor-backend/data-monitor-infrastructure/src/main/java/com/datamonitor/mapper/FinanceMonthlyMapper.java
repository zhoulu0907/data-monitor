package com.datamonitor.mapper;

import com.mybatisflex.core.BaseMapper;
import com.datamonitor.entity.FinanceMonthlyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 月度财务数据 Mapper
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Mapper
public interface FinanceMonthlyMapper extends BaseMapper<FinanceMonthlyEntity> {
}
