import AppLayout from '@/layout/AppLayout.vue'

const applicationRouter = {
  path: '/application',
  name: 'application',
  component: AppLayout,
  meta: {
    title: 'Agent',
    icon: 'application'
  },
  redirect: '/application/index',
  children: [
    {
      path: '/application/index',
      name: 'application-index',
      meta: {
        title: 'Agent',
        activeMenu: '/application',
        parentPath: '/application',
        parentName: 'application'
      },
      component: () => import('@/views/application/index.vue')
    },
    {
      path: '/application/:id',
      name: 'application-detail',
      meta: {
        title: 'Agent详情',
        icon: 'application',
        activeMenu: '/application',
        parentPath: '/application',
        parentName: 'application'
      },
      hidden: true,
      component: () => import('@/layout/AppDetailLayout.vue'),
      children: [
        {
          path: 'overview',
          name: 'AppOverview',
          meta: {
            title: '概览',
            icon: 'icon-all',
            iconActive: 'icon-all-fill',
            active: 'overview',
            parentPath: '/application/:id',
            parentName: 'application-detail',
          },
          component: () => import('@/views/application-overview/index.vue')
        },
        {
          path: 'setting',
          name: 'application-setting',
          meta: {
            title: 'Agent设置',
            icon: 'icon-setting',
            iconActive: 'icon-setting1',
            active: 'setting',
            parentPath: '/application/:id',
            parentName: 'application-detail',
          },
          component: () => import('@/views/application/ApplicationSetting.vue')
        },
        {
          path: 'hit-test',
          name: 'application-hit-test',
          meta: {
            title: '命中测试',
            icon: 'icon-hit',
            iconActive: 'icon-hit-fill',
            active: 'hit-test',
            parentPath: '/application/:id',
            parentName: 'application-detail',
          },
          component: () => import('@/views/hint-test/index.vue')
        },
        {
          path: 'log',
          name: 'application-log',
          meta: {
            title: '对话日志',
            icon: 'icon-log',
            iconActive: 'icon-log-fill',
            active: 'log',
            parentPath: '/application/:id',
            parentName: 'application-detail',
          },
          component: () => import('@/views/log/index.vue')
        }
      ]
    }
  ]
}

export default applicationRouter
