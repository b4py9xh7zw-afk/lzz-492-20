import request from '@/utils/request'

/**
 * 文件API
 */
export const fileApi = {
  // 上传文件
  upload(file, uploadUserName = '系统', uploadUserId = 1) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('uploadUserName', uploadUserName)
    formData.append('uploadUserId', uploadUserId)
    return request({
      url: '/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  // 预览文件
  preview(id) {
    return `${import.meta.env.VITE_API_BASE_URL || '/api'}/file/preview/${id}`
  },
  // 删除文件
  delete(id) {
    return request({
      url: `/file/${id}`,
      method: 'delete'
    })
  },
  // 分页查询
  page(params) {
    return request({
      url: '/file/page',
      method: 'get',
      params
    })
  }
}
