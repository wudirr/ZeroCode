import axiosInstance from '@/util/AxiosUtil.ts'
import type { AxiosRequestConfig } from 'axios'

export type RequestOptions = AxiosRequestConfig

export default function request<T = any>(url: string, options?: RequestOptions): Promise<T> {
  const { method = 'GET', params, data, headers, ...rest } = options || {}

  return axiosInstance.request({
    url,
    method: method as AxiosRequestConfig['method'],
    params,
    data,
    headers,
    ...rest,
  }) as Promise<T>
}
