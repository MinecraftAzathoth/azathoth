import zh from '../locales/zh.json'
import en from '../locales/en.json'

export default defineI18nConfig(() => ({
  legacy: false,
  locale: 'zh',
  fallbackLocale: 'zh',
  messages: {
    zh,
    en,
  },
}))
