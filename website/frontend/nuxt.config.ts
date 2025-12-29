// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },

  modules: [
    '@nuxt/content',
    '@nuxt/image',
    '@nuxtjs/i18n',
    '@nuxtjs/tailwindcss',
    '@nuxtjs/color-mode',
    '@pinia/nuxt',
    '@vueuse/nuxt',
  ],

  css: ['~/assets/css/main.css'],

  typescript: {
    strict: true,
  },

  colorMode: {
    classSuffix: '',
  },

  i18n: {
    locales: ['zh', 'en'],
    defaultLocale: 'zh',
    vueI18n: './i18n.config.ts',
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
