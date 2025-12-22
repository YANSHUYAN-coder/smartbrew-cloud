<template>
	<view class="security-container">
        <!-- 顶部占位，如果不需要导航栏背景可去掉 -->
		<view class="status-placeholder"></view>
		<view class="section-group">
			<view class="cell-item">
				<view class="cell-left">
					<text class="cell-title">绑定手机号</text>
				</view>
				<view class="cell-right">
					<text class="cell-tip">{{ maskedPhone }}</text>
				</view>
			</view>
		</view>

		<view class="section-group">
			<view class="cell-item" hover-class="cell-hover" @click="showPasswordModal = true">
				<view class="cell-left">
					<text class="cell-title">修改登录密码</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
		</view>

		<view class="section-group">
			<view class="cell-item" hover-class="cell-hover" @click="handleLogoff">
				<view class="cell-left">
					<text class="cell-title">注销账号</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
		</view>

		<!-- 修改密码弹窗 -->
		<view class="modal-mask" v-if="showPasswordModal" @click="closeModal">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">修改密码</text>
					<text class="close-btn" @click="closeModal">×</text>
				</view>
				<view class="form-body">
					<view class="input-group">
						<input type="password" v-model="pwdForm.oldPassword" placeholder="请输入原密码" placeholder-class="placeholder" />
					</view>
					<view class="input-group">
						<input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码" placeholder-class="placeholder" />
					</view>
					<view class="input-group">
						<input type="password" v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" placeholder-class="placeholder" />
					</view>
				</view>
				<view class="modal-footer">
					<button class="confirm-btn" :loading="loading" @click="submitPassword">确认修改</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed, reactive } from 'vue'
	import { request } from '@/utils/request.js'
	import { useUserStore } from '@/store/user.js'

	const userStore = useUserStore()
	const showPasswordModal = ref(false)
	const loading = ref(false)

	const pwdForm = reactive({
		oldPassword: '',
		newPassword: '',
		confirmPassword: ''
	})

	const maskedPhone = computed(() => {
		const phone = userStore.userInfo?.phone || ''
		if (!phone) return '未绑定'
		return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
	})

	const closeModal = () => {
		showPasswordModal.value = false
		pwdForm.oldPassword = ''
		pwdForm.newPassword = ''
		pwdForm.confirmPassword = ''
	}

	const submitPassword = async () => {
		if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
			return uni.showToast({ title: '请填写完整', icon: 'none' })
		}
		if (pwdForm.newPassword !== pwdForm.confirmPassword) {
			return uni.showToast({ title: '两次新密码输入不一致', icon: 'none' })
		}
		if (pwdForm.newPassword.length < 6) {
			return uni.showToast({ title: '新密码至少6位', icon: 'none' })
		}

		loading.value = true
		try {
			await request({
				url: '/app/member/updatePassword',
				method: 'POST',
				data: {
					oldPassword: pwdForm.oldPassword,
					newPassword: pwdForm.newPassword
				}
			})
			uni.showToast({ title: '密码修改成功', icon: 'success' })
			
			// 修改密码后清除登录态并跳转至登录页
			userStore.clearUser()
			setTimeout(() => {
				uni.reLaunch({
					url: '/pages/login/index'
				})
			}, 1500)
		} catch (e) {
			uni.showToast({ title: e.message || '修改失败', icon: 'none' })
		} finally {
			loading.value = false
		}
	}

	const handleLogoff = () => {
		uni.showModal({
			title: '账号注销',
			content: '注销后账号将无法找回，确定要注销吗？',
			confirmColor: '#ff4d4f',
			success: async (res) => {
				if (res.confirm) {
					try {
						await request({
							url: '/app/member/logoff',
							method: 'POST'
						})
						uni.showToast({ title: '账号已注销', icon: 'success' })
						userStore.clearUser()
						setTimeout(() => {
							uni.reLaunch({ url: '/pages/home/index' })
						}, 1500)
					} catch (e) {
						uni.showToast({ title: '操作失败', icon: 'none' })
					}
				}
			}
		})
	}
</script>

<style lang="scss" scoped>
	.security-container {
		min-height: 100vh;
		background-color: #f5f7fa;
		padding: 20rpx 30rpx;
	}

    /* 顶部状态栏占位 */
	.status-placeholder {
		height: var(--status-bar-height);
		width: 100%;
	}

	.section-group {
		background-color: #ffffff;
		border-radius: 20rpx;
		margin-bottom: 30rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
	}

	.cell-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 34rpx 30rpx;
		background-color: #fff;
	}

	.cell-hover {
		background-color: #f9f9f9;
	}

	.cell-title {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
	}

	.cell-tip {
		font-size: 28rpx;
		color: #999;
	}

	/* 弹窗样式 */
	.modal-mask {
		position: fixed;
		top: 0; left: 0; right: 0; bottom: 0;
		background-color: rgba(0,0,0,0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 999;
	}

	.modal-content {
		width: 85%;
		background-color: #fff;
		border-radius: 30rpx;
		padding: 40rpx;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 40rpx;
		.modal-title {
			font-size: 34rpx;
			font-weight: bold;
			color: #333;
		}
		.close-btn {
			font-size: 40rpx;
			color: #999;
			padding: 10rpx;
		}
	}

	.input-group {
		background-color: #f8f8f8;
		border-radius: 15rpx;
		margin-bottom: 25rpx;
		padding: 20rpx 30rpx;
		input {
			height: 60rpx;
			font-size: 28rpx;
		}
	}

	.placeholder {
		color: #ccc;
	}

	.modal-footer {
		margin-top: 40rpx;
		.confirm-btn {
			background-color: #d4a373;
			color: #fff;
			border-radius: 50rpx;
			font-size: 30rpx;
			height: 88rpx;
			line-height: 88rpx;
			border: none;
		}
	}
</style>

