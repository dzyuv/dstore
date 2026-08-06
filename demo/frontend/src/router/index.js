import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/goods/Home.vue'),
        meta: { public: true, title: '首页' }
      },
      {
        path: 'goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/goods/Detail.vue'),
        meta: { public: true, title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/goods/Cart.vue'),
        meta: { title: '购物车', roles: ['CUSTOMER', 'MERCHANT', 'ADMIN'] }
      },
      {
        path: 'merchant/products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/ProductList.vue'),
        meta: { title: '商品管理', roles: ['MERCHANT', 'ADMIN'] }
      },
      {
        path: 'merchant/products/create',
        name: 'MerchantProductCreate',
        component: () => import('@/views/merchant/ProductEdit.vue'),
        meta: { title: '发布商品', roles: ['MERCHANT', 'ADMIN'] }
      },
      {
        path: 'merchant/products/:id',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/ProductEdit.vue'),
        meta: { title: '编辑商品', roles: ['MERCHANT', 'ADMIN'] }
      },
      {
        path: 'admin/categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductSupervise.vue'),
        meta: { title: '商品监管', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? `${to.meta.title} - ` : '') + 'DStore 商城'
  const userStore = useUserStore()
  if (to.meta.public) return next()
  if (!userStore.isLogin) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  const roles = to.meta.roles
  if (roles && roles.length && !roles.includes(userStore.role)) {
    return next({ path: '/' })
  }
  next()
})

export default router
