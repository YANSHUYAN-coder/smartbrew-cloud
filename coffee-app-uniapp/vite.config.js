import {
	defineConfig
} from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [
		uni()
	],
	server: {
		port: 8080,
		open: true,
		host: '0.0.0.0',
		proxy: {
			'/api': {
				target: 'http://localhost:8081',
				changeOrigin: true, // 建议开启
			}
		}
	},
})