import SettingLayout from '@/layout/SettingLayout.vue'

const settingRouter = {
  path: '/setting',
  name: 'setting',
  component: SettingLayout,
  meta: {
    title: '设置',
    icon: 'setting'
  },
  redirect: '/setting/model',
  children: [
    {
      path: '/setting/model',
      name: 'model',
      meta: {
        title: '模型',
        icon: 'model',
        activeMenu: '/setting',
        parentPath: '/setting',
        parentName: 'setting'
      },
      component: () => import('@/views/model/index.vue')
    }
  ]
}

export default settingRouter
