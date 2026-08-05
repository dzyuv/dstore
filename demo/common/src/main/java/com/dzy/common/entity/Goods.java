package com.dzy.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兼容旧接口的简化商品视图（SKU 维度）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
}
