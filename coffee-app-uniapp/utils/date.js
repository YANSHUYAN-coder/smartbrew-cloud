/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} dateStr - 日期字符串或Date对象
 * @param {string} format - 格式化模式，默认 'YYYY-MM-DD HH:mm'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDateTime = (dateStr, format = 'YYYY-MM-DD HH:mm') => {
	if (!dateStr) return ''
	
	const date = new Date(dateStr)
	if (isNaN(date.getTime())) return ''
	
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	const hours = String(date.getHours()).padStart(2, '0')
	const minutes = String(date.getMinutes()).padStart(2, '0')
	const seconds = String(date.getSeconds()).padStart(2, '0')
	
	return format
		.replace('YYYY', year)
		.replace('MM', month)
		.replace('DD', day)
		.replace('HH', hours)
		.replace('mm', minutes)
		.replace('ss', seconds)
}

/**
 * 格式化日期（不包含时间）
 * @param {string|Date} dateStr - 日期字符串或Date对象
 * @returns {string} 格式化后的日期字符串，格式：YYYY.MM.DD
 */
export const formatDate = (dateStr) => {
	if (!dateStr) return '长期有效'
	const date = new Date(dateStr)
	if (isNaN(date.getTime())) return '长期有效'
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${year}.${month}.${day}`
}

/**
 * 格式化时间（仅时间部分）
 * @param {string|Date} dateStr - 日期字符串或Date对象
 * @returns {string} 格式化后的时间字符串，格式：HH:mm
 */
export const formatTime = (dateStr) => {
	if (!dateStr) return ''
	const date = new Date(dateStr)
	if (isNaN(date.getTime())) return ''
	const hours = String(date.getHours()).padStart(2, '0')
	const minutes = String(date.getMinutes()).padStart(2, '0')
	return `${hours}:${minutes}`
}

