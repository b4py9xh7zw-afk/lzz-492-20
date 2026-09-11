import request from '@/utils/request'

/**
 * 用户API
 */
export const userApi = {
  // 登录
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    })
  },
  // 分页查询
  page(params) {
    return request({
      url: '/user/page',
      method: 'get',
      params
    })
  },
  // 新增用户
  save(data) {
    return request({
      url: '/user',
      method: 'post',
      data
    })
  },
  // 更新用户
  update(id, data) {
    return request({
      url: `/user/${id}`,
      method: 'put',
      data
    })
  },
  // 删除用户
  delete(id) {
    return request({
      url: `/user/${id}`,
      method: 'delete'
    })
  },
  // 重置密码
  resetPassword(id, newPassword) {
    return request({
      url: `/user/${id}/reset-password`,
      method: 'put',
      data: { newPassword }
    })
  }
}
