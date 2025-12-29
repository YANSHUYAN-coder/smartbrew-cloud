import {
	defineConfig
} from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [
		uni()
	],
	resolve: {
		alias: {
			'@': path.resolve(__dirname)
		}
	},
	server: {
		port: 8083,
		open: true,
		host: '0.0.0.0',
		proxy: {
			'/api': {
				target: 'http://localhost:8081',
				changeOrigin: true
			}
		}
	},
})