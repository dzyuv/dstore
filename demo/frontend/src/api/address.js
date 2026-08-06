import request from './request'

export function listAddresses() {
  return request.get('/users/me/addresses')
}

export function getDefaultAddress() {
  return request.get('/users/me/addresses/default')
}

export function addAddress(data) {
  return request.post('/users/me/addresses', data)
}

export function updateAddress(id, data) {
  return request.put(`/users/me/addresses/${id}`, data)
}

export function deleteAddress(id) {
  return request.delete(`/users/me/addresses/${id}`)
}

export function setDefaultAddress(id) {
  return request.put(`/users/me/addresses/${id}/default`)
}
