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
        // 商家入驻：公开页，带顶部导航
        path: 'merchant/apply',
        name: 'MerchantApply',
        component: () => import('@/views/user/MerchantApply.vue'),
        meta: { public: true, title: '商家入驻' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/goods/Cart.vue'),
        meta: { title: '购物车', roles: ['CUSTOMER', 'MERCHANT', 'ADMIN'] }
      },
      {
        path: 'address',
        name: 'AddressList',
        component: () => import('@/views/user/AddressList.vue'),
        meta: { title: '地址管理', roles: ['CUSTOMER', 'MERCHANT', 'ADMIN'] }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '我的订单', roles: ['CUSTOMER', 'MERCHANT', 'ADMIN'] }
      },
      {
        path: 'orders/checkout',
        name: 'OrderCheckout',
        component: () => import('@/views/order/Settlement.vue'),
        meta: { title: '订单结算', roles: ['CUSTOMER', 'MERCHANT', 'ADMIN'] }
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
        path: 'merchant/stores',
        name: 'MerchantStores',
        component: () => import('@/views/user/StoreList.vue'),
        meta: { title: '门店管理', roles: ['MERCHANT', 'ADMIN'] }
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
      },
      {
        path: 'admin/merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/MerchantAudit.vue'),
        meta: { title: '商家审核', roles: ['ADMIN'] }
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
