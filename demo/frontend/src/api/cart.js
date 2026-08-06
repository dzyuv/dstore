import request from './request'

export function getCart() {
  return request.get('/cart')
}

export function addToCart(data) {
  return request.post('/cart/items', data)
}

export function updateCartItem(id, data) {
  return request.put(`/cart/items/${id}`, data)
}

export function deleteCartItem(id) {
  return request.delete(`/cart/items/${id}`)
}

export function deleteCartBatch(ids) {
  return request.delete('/cart/items', { data: { ids } })
}

export function selectAllCart(selected) {
  return request.put('/cart/select-all', null, { params: { selected } })
}

export function selectStoreCart(storeId, selected) {
  return request.put('/cart/select-store', null, { params: { storeId, selected } })
}

export function getSelectedCart() {
  return request.get('/cart/selected')
}

/** 下单成功后清除已选中项 */
export function clearSelectedCart() {
  return request.delete('/cart/selected')
}
