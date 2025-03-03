import AppLayout from '@/layout/AppLayout.vue'

const applicationRouter = {
  path: '/application',
  name: 'application',
  component: AppLayout,
  meta: {
    title: '应用',
    icon: 'application'
  },
  redirect: '/application/index',
  children: [
    {
      path: '/application/index',
      name: 'application-index',
      meta: {
        title: '应用',
        activeMenu: '/application',
        parentPath: '/application',
        parentName: 'application'
      },
      component: () => import('@/views/application/index.vue')
    },
    {
      path: '/application/:id/:type',
      name: 'application-detail',
      meta: {
        title: '应用详情',
        icon: 'application',
        activeMenu: '/application',
        parentPath: '/application',
        parentName: 'application'
      },
      hidden: true,
      component: () => import('@/layout/AppDetailLayout.vue'),
      children: [
        {
          path: '/application/:id/:type/setting',
          name: 'application-setting',
          meta: {
            title: '应用设置',
            icon: 'application-setting',
            activeMenu: '/application/:id/:type',
            parentPath: '/application/:id/:type',
            parentName: 'application-detail'
          },
          component: () => import('@/views/application/ApplicationSetting.vue')
        }
      ]
    }
  ]
}

export default applicationRouter
