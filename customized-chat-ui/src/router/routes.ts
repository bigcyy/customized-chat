import  type { RouteRecordRaw } from 'vue-router'

const modules: any = import.meta.glob('./modules/*.ts', { eager: true })
const rolesRoutes: RouteRecordRaw[] = [...Object.keys(modules).map((key) => modules[key].default)]

export const routes: Array<RouteRecordRaw> =[
  {
    path: '/',
    name: 'home',
    redirect: '/application',
    children: [...rolesRoutes]
  },
]