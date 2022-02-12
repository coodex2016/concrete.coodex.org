import { defineUserConfig } from 'vuepress'
import type { DefaultThemeOptions } from 'vuepress'

export default defineUserConfig<DefaultThemeOptions>({

    lang: 'zn-CN',
    // title: "change me",
    // base: "/docs/",
    themeConfig: {
        sidebar: [
            {
                text: '项目简介',
                link: "/"
            }, {
                text: 'A. 模块清单',
                collapsible: true,
                children: require("./modules.json")
            }, {
                text: 'B. 错误信息',
                link: '/errorInfo',
            }, {
                text: 'C. POJO',
                collapsible: true,
                children: require("./pojos.json")
            }
        ],
    },
    plugins: [
        ['@vuepress/plugin-search'],
    ],
})