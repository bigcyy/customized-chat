import Result from '@/request/Result'
import type { Ref } from 'vue'
import { get, post, put, del } from '@/request'
import type { ApplicationForm } from '@/api/type/application'
/**
 * 创建应用
 */
const createApplication: (data: ApplicationForm, loading?: Ref<boolean>) => Promise<Result<any>> = (
  data,
  loading
) => {
  return post('/application', data, {}, loading)
}

export default { createApplication }
