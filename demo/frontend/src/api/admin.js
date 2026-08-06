import request from './request'

export function getPendingMerchants() {
  return request.get('/admin/merchants/pending')
}

export function auditMerchant(data) {
  return request.put('/admin/merchants/audit', data)
}

export function getMerchants(params) {
  return request.get('/admin/merchants', { params })
}

export function updateMerchantStatus(data) {
  return request.put('/admin/merchants/status', data)
}

export function getUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(data) {
  return request.put('/admin/users/status', data)
}

export function getOperationLogs(params) {
  return request.get('/admin/operation-logs', { params })
}
