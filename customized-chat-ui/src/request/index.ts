import axios, { type AxiosRequestConfig } from 'axios'
import { MsgError } from '@/utils/message'
import type { NProgress } from 'nprogress'
import type { Ref } from 'vue'
import { ref, type WritableComputedRef } from 'vue'
import Result from './Result'

const axiosApiPrefix = '/api/api/v1'

const axiosConfig = {
  baseURL: axiosApiPrefix,
  withCredentials: false,
  timeout: 600000,
  headers: {}
}

const instance = axios.create(axiosConfig)
/* 设置请求拦截器 */
instance.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    return config
  },
  (err: any) => {
    return Promise.reject(err)
  }
)

//设置响应拦截器
instance.interceptors.response.use(
  (response: any) => {
    const result = response.data
    if (!result.success) {
      MsgError(result.message || '请求失败')
      return Promise.reject(result)
    }
    return result
  },
  (err: any) => {
    if (err.code === 'ECONNABORTED') {
      MsgError(err.message)
      console.error(err)
    }

    return Promise.reject(err)
  }
)

export const request = instance

/* 简化请求方法，统一处理返回结果，并增加loading处理，这里以{success,data,message}格式的返回值为例，具体项目根据实际需求修改 */
const promise: (
  request: Promise<any>,
  loading?: NProgress | Ref<boolean> | WritableComputedRef<boolean>
) => Promise<Result<any>> = (request, loading = ref(false)) => {
  return new Promise((resolve, reject) => {
    if ((loading as NProgress).start) {
      ;(loading as NProgress).start()
    } else {
      ;(loading as Ref).value = true
    }
    request
      .then((response) => {
        resolve(response)
      })
      .catch((error) => {
        reject(error)
      })
      .finally(() => {
        if ((loading as NProgress).start) {
          ;(loading as NProgress).done()
        } else {
          ;(loading as Ref).value = false
        }
      })
  })
}

/**
 * 发送get请求   一般用来请求资源
 * @param url    资源url
 * @param params 参数
 * @param loading loading
 * @returns 异步promise对象
 */
export const get: (
  url: string,
  params?: unknown,
  loading?: NProgress | Ref<boolean>,
  timeout?: number
) => Promise<Result<any>> = (
  url: string,
  params: unknown,
  loading?: NProgress | Ref<boolean>,
  timeout?: number
) => {
  return promise(request({ url: url, method: 'get', params, timeout: timeout }), loading)
}

/**
 * faso post请求 一般用来添加资源
 * @param url    资源url
 * @param params 参数
 * @param data   添加数据
 * @param loading loading
 * @returns 异步promise对象
 */
export const post: (
  url: string,
  data?: unknown,
  params?: unknown,
  loading?: NProgress | Ref<boolean>,
  timeout?: number
) => Promise<Result<any> | any> = (url, data, params, loading, timeout) => {
  return promise(request({ url: url, method: 'post', data, params, timeout }), loading)
}

/**|
 * 发送put请求 用于修改服务器资源
 * @param url     资源地址
 * @param params  params参数地址
 * @param data    需要修改的数据
 * @param loading 进度条
 * @returns
 */
export const put: (
  url: string,
  data?: unknown,
  params?: unknown,
  loading?: NProgress | Ref<boolean>,
  timeout?: number
) => Promise<Result<any>> = (url, data, params, loading, timeout) => {
  return promise(request({ url: url, method: 'put', data, params, timeout }), loading)
}

/**
 * 删除
 * @param url     删除url
 * @param params  params参数
 * @param loading 进度条
 * @returns
 */
export const del: (
  url: string,
  params?: unknown,
  data?: unknown,
  loading?: NProgress | Ref<boolean>,
  timeout?: number
) => Promise<Result<any>> = (url, params, data, loading, timeout) => {
  return promise(request({ url: url, method: 'delete', params, data, timeout }), loading)
}

/**
 * SSE流处理 - 处理Server-Sent Events
 * @param url url地址
 * @param data 请求body
 * @param onMessage 接收到消息时的回调
 * @param onError 错误回调
 * @param onComplete 完成回调
 * @returns 控制对象，包含abort方法用于取消请求
 */
export const postSSEStream = (
  url: string,
  data?: unknown,
  onMessage?: (data: string) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
): {
  abort: () => void,
  promise: Promise<any>
} => {
  const headers: HeadersInit = { 
    'Content-Type': 'application/json',
    'Accept': 'text/event-stream',
    'Cache-Control': 'no-cache'
  }

  const controller = new AbortController()
  
  const fetchPromise = fetch(`${axiosApiPrefix}${url}`, {
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined,
    headers: headers,
    signal: controller.signal
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    
    if (!reader) {
      throw new Error('Unable to get response reader')
    }
    
    let tempResult = ''
    const readStream = () => {
      reader.read().then(({ done, value }) => {
        if (done) {
          onComplete?.()
          return
        }
        
        // 解码数据
        let chunk = decoder.decode(value, { stream: true })
        console.log("readStream chunk", chunk)
        tempResult = tempResult + chunk
        let split = tempResult.match(/data:.*}\n\n/g)
        if(split){
          chunk = split.join('')
          tempResult = tempResult.replace(chunk, '')
        }else{
          readStream()
        }

        if(chunk && chunk.startsWith('data:')){
          if(split){
            for (const index in split) {
              const dataContent = split[index].replace('data:', '').trim()
              try {
                // 尝试解析为JSON（临时聊天格式）
                const jsonChunk = JSON.parse(dataContent)
                if (jsonChunk.message) {
                  onMessage?.(jsonChunk.message)
                } else {
                  onMessage?.(dataContent)
                }
              } catch {
                // 如果不是JSON，直接使用内容（正式聊天格式）
                onMessage?.(dataContent)
              }
            }
          }
        }
        // 继续读取
        readStream()
      }).catch(error => {
        if (error.name !== 'AbortError') {
          onError?.(error)
        }
      })
    }
    
    readStream()
  })
  .catch(error => {
    if (error.name !== 'AbortError') {
      onError?.(error)
    }
  })
  
  return {
    abort: () => controller.abort(),
    promise: fetchPromise
  }
}
