import { type App } from 'vue'
import IconFont from './icons/index.vue'
import AiChat from './ai-chat/index.vue'
import ColorAvater from './avaters/coloer-avater.vue'
import UserAvater from './avaters/user-avater.vue'

export default {
  install(app: App) {
    app.component(IconFont.name ?? 'IconFont', IconFont)
    app.component(AiChat.name ?? 'AiChat', AiChat)
    app.component(ColorAvater.name ?? 'ColorAvater', ColorAvater)
    app.component(UserAvater.name ?? 'UserAvater', UserAvater)
  }
}
