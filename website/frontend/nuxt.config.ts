// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },

  modules: [
    '@nuxt/content',
    '@nuxt/image',
    '@nuxtjs/i18n',
    '@pinia/nuxt',
    '@vueuse/nuxt',
  ],

  typescript: {
    strict: true,
  },

  i18n: {
    locales: [
      { code: 'zh', name: '简体中文', file: 'zh.json' },
      { code: 'en', name: 'English', file: 'en.json' },
    ],
    defaultLocale: 'zh',
    lazy: true,
    langDir: 'locales',
  },

  content: {
    highlight: {
      theme: 'github-dark',
    },
  },

  nitro: {
    preset: 'node-server',
  },

  app: {
    head: {
      title: 'Azathoth - Minecraft MMORPG Framework',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Azathoth - A high-performance Minecraft MMORPG server framework' },
      ],
    },
  },

  compatibilityDate: '2024-12-29',
});
