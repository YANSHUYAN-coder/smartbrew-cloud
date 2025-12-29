import request from '@/utils/request'

export interface CouponQuery {
    pageNum: number
    pageSize: number
    name?: string
    type?: number
}

export interface Coupon {
    id?: number
    type: number
    name: string
    platform: number
    count: number
    amount: number
    perLimit: number
    minPoint: number
    startTime: string
    endTime: string
    useType: number
    note?: string
    publishCount?: number
    useCount?: number
    receiveCount?: number
    enableTime?: string
    code?: string
    memberLevel?: number
    validDays?: number
    points?: number
}

// 获取优惠券列表
export function getCouponList(params: CouponQuery) {
    return request({
        url: '/admin/coupon/list',
        method: 'get',
        params
    })
}

// 创建优惠券
export function createCoupon(data: Coupon) {
    return request({
        url: '/admin/coupon/create',
        method: 'post',
        data
    })
}

// 更新优惠券
export function updateCoupon(id: number, data: Coupon) {
    return request({
        url: `/admin/coupon/update/${id}`,
        method: 'post',
        data
    })
}

// 删除优惠券
export function deleteCoupon(id: number) {
    return request({
        url: `/admin/coupon/delete/${id}`,
        method: 'post'
    })
}

// 获取详情
export function getCouponDetail(id: number) {
    return request({
        url: `/admin/coupon/${id}`,
        method: 'get'
    })
}