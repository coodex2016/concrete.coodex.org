import {defineUserConfig} from 'vuepress'
import {defaultTheme} from '@vuepress/theme-default'
import {searchPlugin} from "@vuepress/plugin-search"

export default defineUserConfig({

    lang: 'zh-CN',
    port: 4000,
    // title: "change me",
    // base: "/docs/",
    theme: defaultTheme({
        sidebar: [
            {
                text: '项目简介',
                link: "/"
            },
            {
                text: 'A. 模块清单',
                collapsible: true,
                children: require("./modules.json"),
                link: '/moduleList',
            },
            {
                text: 'B. 错误信息',
                link: '/errorInfo',
            },
            {
                text: 'C. POJO',
                collapsible: true,
                children: require("./pojos.json")
            }
        ],
    }),
    plugins: [
        searchPlugin({}),
    ],
})