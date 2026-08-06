import request from './request'

/** 创建订单 */
export function createOrder(data) {
  return request.post('/orders', data)
}

export function listOrders() {
  return request.get('/orders')
}

export function getOrderDetail(orderNo) {
  return request.get(`/orders/${orderNo}`)
}

export function cancelOrder(orderNo, reason) {
  return request.post(`/orders/${orderNo}/cancel`, null, { params: { reason } })
}

export function confirmOrder(orderNo) {
  return request.post(`/orders/${orderNo}/confirm`)
}

export function createPayment(data) {
  return request.post('/payments/create', data)
}

/** 模拟支付回调（演示） */
export function paymentCallback(paymentNo, success = true) {
  return request.get(`/payments/callback/${paymentNo}`, { params: { success } })
}

export function refundPayment(data) {
  return request.post('/payments/refund', data)
}

export function getDelivery(orderNo) {
  return request.get(`/deliveries/${orderNo}`)
}

export function updateDelivery(orderNo, data) {
  return request.put(`/deliveries/${orderNo}`, data)
}

export function createReview(data) {
  return request.post('/reviews', data)
}

export function listProductReviews(productId) {
  return request.get(`/reviews/product/${productId}`)
}

export function hideReview(id) {
  return request.put(`/reviews/${id}/hide`)
}

/** 商家配送管理 */
export function getMerchantOrders() {
  return request.get('/orders/merchant')
}

export function updateDeliveryStatus(orderNo, data) {
  return request.put(`/orders/merchant/${orderNo}/delivery`, data)
}
