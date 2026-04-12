import axios from 'axios'

/** Axios 实例，统一配置基础路径和超时 */
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

/** 请求拦截器：自动携带 JWT Token */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/** 响应拦截器：统一提取 data + 401 跳转登录 */
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    const msg = error.response?.data?.message || '网络请求失败'
    console.error('[API 错误]', msg)
    return Promise.reject(new Error(msg))
  },
)

export default request
