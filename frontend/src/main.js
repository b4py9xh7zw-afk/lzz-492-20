import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './style.css'

// PC端使用Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// H5端使用Vant
import Vant from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(Vant)
app.use(router)
app.use(store)
app.mount('#app')
