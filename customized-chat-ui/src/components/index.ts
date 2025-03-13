import { type App } from 'vue'
import IconFont from './icons/index.vue'

export default {
  install(app: App) {
    app.component(IconFont.name ?? 'IconFont', IconFont)
  }
}
