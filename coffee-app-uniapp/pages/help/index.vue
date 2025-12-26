<template>
    <view class="container">
        <!-- 页面标题 (自定义导航栏时使用，原生导航栏可忽略) -->
        <view class="page-header" :style="{ paddingTop: statusBarHeight + 'px' }">
            <text class="page-title">帮助中心</text>
        </view>

        <!-- 常见问题分类 -->
        <view class="section-title">常见问题</view>
        <view class="faq-list">
            <view class="faq-item" v-for="(item, index) in faqList" :key="index" @click="toggleFaq(index)">
                <view class="faq-header">
                    <text class="question">{{ item.question }}</text>
                    <uni-icons :type="item.isOpen ? 'top' : 'bottom'" size="14" color="#ccc"></uni-icons>
                </view>
                <view class="faq-content" v-if="item.isOpen">
                    <text class="answer">{{ item.answer }}</text>
                </view>
            </view>
        </view>

        <!-- 底部联系方式 -->
        <view class="contact-section">
            <view class="contact-btn" hover-class="btn-hover" @click="handleOnlineService">
                <uni-icons type="chat-filled" size="24" color="#d4a373"></uni-icons>
                <text class="btn-text">在线客服</text>
                <text class="btn-desc">09:00-22:00</text>
            </view>
            <view class="divider"></view>
            <view class="contact-btn" hover-class="btn-hover" @click="handleCallPhone">
                <uni-icons type="phone-filled" size="24" color="#d4a373"></uni-icons>
                <text class="btn-text">电话客服</text>
                <text class="btn-desc">400-888-8888</text>
            </view>
        </view>

        <view class="feedback-link" @click="navToFeedback">
            <text>遇到其他问题？</text>
            <text class="link-text">提交意见反馈</text>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import {
    getStatusBarHeight
} from '@/utils/system.js'

const statusBarHeight = ref(0)

// FAQ 数据列表
const faqList = ref([
    {
        question: '如何获取积分？',
        answer: '您在 SmartBrew App 或小程序下单消费，每实付 1 元即可获得 1 积分。积分将在订单完成后自动发放至您的账户。',
        isOpen: false
    },
    {
        question: '下单后可以取消订单吗？',
        answer: '如果订单状态为“待制作”，您可以直接在订单详情页取消并退款；如果订单已进入“制作中”，为保证咖啡口感和减少浪费，暂时无法自助取消，请联系客服处理。',
        isOpen: false
    },
    {
        question: '会员等级有哪些权益？',
        answer: 'SmartBrew 会员分为普通会员、白银会员和黄金会员。等级越高，积分倍率越高，且享有专属生日礼、免配送费等特权。详情可在“会员中心”查看。',
        isOpen: false
    },
    {
        question: '如何修改绑定的手机号？',
        answer: '为了您的账户安全，App 内暂不支持自助修改手机号。如需换绑，请点击下方的“在线客服”联系人工处理。',
        isOpen: false
    },
    {
        question: '优惠券怎么使用？',
        answer: '在结算页面，点击“优惠券”栏目，系统会自动推荐当前可用的最优优惠券。您也可以手动选择其他适用的优惠券。',
        isOpen: false
    }
])

// 切换折叠状态
const toggleFaq = (index) => {
    faqList.value[index].isOpen = !faqList.value[index].isOpen
}

// 在线客服
const handleOnlineService = () => {
    uni.showToast({
        title: '正在连接客服...',
        icon: 'loading'
    })
    // 实际场景可跳转至客服IM页面
}

// 拨打电话
const handleCallPhone = () => {
    uni.makePhoneCall({
        phoneNumber: '4008888888'
    })
}

// 跳转反馈页面
const navToFeedback = () => {
    // uni.navigateTo({ url: '/pages/help/feedback' })
    uni.showToast({ title: '反馈功能开发中', icon: 'none' })
}

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.container {
    min-height: 100vh;
    background-color: #f4f6f8;
    padding: 30rpx;
    padding-bottom: 60rpx;
}

.page-header {
    // margin-bottom: 30rpx;
    justify-content: center;
    align-items: center;
    display: flex;

    .page-title {
        font-size: 38rpx;
        font-weight: bold;
        color: #333;
    }
}

/* 头部区域 */
.header-section {
    background-color: #fff;
    padding: 40rpx 30rpx;
    border-radius: 24rpx;
    margin-bottom: 40rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);

    .header-title {
        font-size: 40rpx;
        font-weight: bold;
        color: #333;
        display: block;
    }

    .header-subtitle {
        font-size: 28rpx;
        color: #999;
        margin-top: 10rpx;
        display: block;
        margin-bottom: 30rpx;
    }

    .search-box {
        background-color: #f5f7fa;
        height: 80rpx;
        border-radius: 40rpx;
        display: flex;
        align-items: center;
        padding: 0 30rpx;

        .search-text {
            font-size: 28rpx;
            color: #bbb;
            margin-left: 16rpx;
        }
    }
}

.section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    padding-left: 10rpx;
}

/* FAQ 列表 */
.faq-list {
    background-color: #fff;
    border-radius: 24rpx;
    margin-bottom: 40rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.02);
    overflow: hidden;
}

.faq-item {
    border-bottom: 1rpx solid #f9f9f9;

    &:last-child {
        border-bottom: none;
    }

    .faq-header {
        padding: 30rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .question {
            font-size: 28rpx;
            color: #333;
            font-weight: 500;
            flex: 1;
            margin-right: 20rpx;
        }
    }

    .faq-content {
        padding: 0 30rpx 30rpx;

        .answer {
            font-size: 26rpx;
            color: #666;
            line-height: 1.6;
        }
    }
}

/* 底部联系区域 */
.contact-section {
    background-color: #fff;
    border-radius: 24rpx;
    padding: 30rpx 0;
    display: flex;
    align-items: center;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);

    .contact-btn {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .btn-text {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
            margin-top: 10rpx;
        }

        .btn-desc {
            font-size: 22rpx;
            color: #999;
            margin-top: 4rpx;
        }
    }

    .divider {
        width: 2rpx;
        height: 60rpx;
        background-color: #eee;
    }

    .btn-hover {
        opacity: 0.7;
    }
}

/* 底部反馈链接 */
.feedback-link {
    text-align: center;
    margin-top: 40rpx;
    font-size: 24rpx;
    color: #999;

    .link-text {
        color: #d4a373;
        margin-left: 10rpx;
        text-decoration: underline;
    }
}
</style>