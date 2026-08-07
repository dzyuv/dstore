import request from './request'

/** 消费者：上架商品列表/搜索 */
export function searchGoods(params) {
  return request.get('/goods/list', { params })
}

/** 消费者：商品详情 */
export function getGoodsDetail(productId) {
  return request.get(`/goods/detail/${productId}`)
}

/** 商家：商品列表 */
export function merchantGoodsList(params) {
  return request.get('/goods/merchant/list', { params })
}

/** 商家：商品详情 */
export function merchantGoodsDetail(productId) {
  return request.get(`/goods/merchant/${productId}`)
}

/** 商家：创建商品 */
export function createGoods(data) {
  return request.post('/goods', data)
}

/** 商家：更新商品 */
export function updateGoods(data) {
  return request.put('/goods', data)
}

/** 商家：上下架 */
export function changeGoodsStatus(productId, status) {
  return request.put(`/goods/${productId}/status`, { status })
}

/** 商家：删除商品 */
export function deleteGoods(productId) {
  return request.delete(`/goods/${productId}`)
}

/** 商家：新增 SKU */
export function addSku(productId, data) {
  return request.post(`/goods/${productId}/skus`, data)
}

/** 商家：修改 SKU */
export function updateSku(productId, data) {
  return request.put(`/goods/${productId}/skus`, data)
}

/** 商家：删除 SKU */
export function deleteSku(productId, skuId) {
  return request.delete(`/goods/${productId}/skus/${skuId}`)
}

/** 商家：调整库存 */
export function adjustStock(data) {
  return request.post('/goods/stock/adjust', data)
}

/** 库存流水 */
export function getStockLogs(skuId, limit = 50) {
  return request.get(`/goods/stock-logs/${skuId}`, { params: { limit } })
}

/** 管理员：商品列表 */
export function adminGoodsList(params) {
  return request.get('/goods/admin/list', { params })
}

/** 管理员：强制下架 */
export function platformOff(productId) {
  return request.put(`/goods/admin/${productId}/platform-off`)
}

/** 管理员：恢复平台下架 */
export function restoreFromPlatformOff(productId) {
  return request.put(`/goods/admin/${productId}/restore`)
}
