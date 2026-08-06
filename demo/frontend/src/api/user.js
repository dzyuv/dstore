import request from './request'

export function sendSms(data) {
  return request.post('/users/sms/send', data)
}

export function login(data) {
  return request.post('/users/login', data)
}

export function register(data) {
  return request.post('/users', data)
}

export function getMe() {
  return request.get('/users/me')
}

/** 商家门店列表（创建商品时选择门店） */
export function listStores() {
  return request.get('/merchant/stores')
}

export { listStores as getMerchantStores }
