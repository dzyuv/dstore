package com.dzy.goodsprovider8090.service;

import com.dzy.goodsprovider8090.dto.StockChangeRequest;

public interface StockService {

    /** 下单锁定库存 */
    void lock(StockChangeRequest request);

    /** 取消/超时释放锁定 */
    void unlock(StockChangeRequest request);

    /** 支付成功：物理库存与锁定库存同时扣减 */
    void deduct(StockChangeRequest request);

    /** 退款恢复物理库存 */
    void restore(StockChangeRequest request);
}
