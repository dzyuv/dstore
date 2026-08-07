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

export function resetPassword(data) {
  return request.post('/users/password/reset', data)
}

export function getMe() {
  return request.get('/users/me')
}
