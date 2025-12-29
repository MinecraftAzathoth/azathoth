package com.azathoth.website.module.market

/**
 * 资源类型
 */
enum class ResourceType {
    PLUGIN,      // 游戏插件
    MODULE,      // 功能模块
    SERVICE,     // 独立服务
    TEMPLATE,    // 项目模板
    THEME,       // 后台主题
    TOOL         // 开发工具
}

/**
 * 授权类型
 */
enum class LicenseType {
    FREE_OPEN_SOURCE,   // 免费开源
    FREE_CLOSED_SOURCE, // 免费闭源
    PAID_PERPETUAL,     // 付费买断
    PAID_SUBSCRIPTION   // 付费订阅
}

/**
 * 审核状态
 */
enum class ResourceStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

/**
 * 排序方式
 */
enum class MarketSortBy {
    RELEVANCE,
    DOWNLOADS,
    RATING,
    UPDATED,
    CREATED
}

/**
 * 排序顺序
 */
enum class SortOrder {
    ASC, DESC
}
