package com.dzy.goodsprovider8090.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockLog {
    private Long id;
    private Long skuId;
    private String changeType;
    private Integer changeQty;
    private Integer physicalAfter;
    private Integer lockedAfter;
    private String bizNo;
    private String remark;
    private LocalDateTime createdAt;
}