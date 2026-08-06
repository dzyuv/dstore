import request from './request'

export function applyMerchant(data) {
  return request.post('/merchant/apply', data)
}

export function reapplyMerchant(data) {
  return request.put('/merchant/reapply', data)
}

export function listStores() {
  return request.get('/merchant/stores')
}

export function getStore(storeId) {
  return request.get(`/merchant/stores/${storeId}`)
}

export function createStore(data) {
  return request.post('/merchant/stores', data)
}

export function updateStore(data) {
  return request.put('/merchant/stores', data)
}

export function deleteStore(storeId) {
  return request.delete(`/merchant/stores/${storeId}`)
}
