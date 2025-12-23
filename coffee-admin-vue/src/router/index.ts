import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页', icon: 'House' }
        },
        {
          path: 'product',
          name: 'product',
          meta: { title: '商品管理', icon: 'Goods' },
          children: [
            {
              path: 'list',
              name: 'productList',
              component: () => import('@/views/product/list.vue'),
              meta: { title: '商品列表' }
            },
            {
              path: 'add',
              name: 'productAdd',
              component: () => import('@/views/product/add.vue'),
              meta: { title: '发布商品' }
            }
          ]
        },
        {
          path: 'category',
          name: 'category',
          meta: { title: '分类管理', icon: 'Grid' },
          children: [
            {
              path: 'list',
              name: 'categoryList',
              component: () => import('@/views/category/list.vue'),
              meta: { title: '分类列表' }
            }
          ]
        },
        {
          path: 'order',
          name: 'order',
          meta: { title: '订单管理', icon: 'List' },
          children: [
            {
              path: 'list',
              name: 'orderList',
              component: () => import('@/views/order/list.vue'),
              meta: { title: '订单列表' }
            }
          ]
        },
        {
          path: 'member',
          name: 'member',
          meta: { title: '会员管理', icon: 'User' },
          children: [
            {
              path: 'list',
              name: 'memberList',
              component: () => import('@/views/member/list.vue'),
              meta: { title: '会员列表' }
            }
          ]
        },
        {
          path: 'marketing',
          name: 'marketing',
          meta: { title: '营销中心', icon: 'Present' },
          children: [
            {
              path: 'coupon',
              name: 'couponList',
              component: () => import('@/views/marketing/coupon.vue'),
              meta: { title: '优惠券管理' }
            }
          ]
        },
        {
          path: 'system',
          name: 'system',
          meta: { title: '系统管理', icon: 'Setting' },
          children: [
            {
              path: 'role',
              name: 'sysRole',
              component: () => import('@/views/system/role/index.vue'),
              meta: { title: '角色管理' }
            },
            {
              path: 'menu',
              name: 'sysMenu',
              component: () => import('@/views/system/menu/index.vue'),
              meta: { title: '菜单管理' }
            },
            {
              path: 'member-level',
              name: 'sysMemberLevel',
              component: () => import('@/views/system/memberLevel/index.vue'),
              meta: { title: '会员等级管理' }
            },
            {
              path: 'ai',
              name: 'sysAi',
              component: () => import('@/views/system/ai/index.vue'),
              meta: { title: 'AI 知识库', icon: 'Cpu' }
            }
          ]
        }
      ]
    }
  ]
})

export default router

