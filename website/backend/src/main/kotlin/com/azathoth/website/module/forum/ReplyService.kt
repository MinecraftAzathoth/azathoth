package com.azathoth.website.module.forum

/**
 * 回复服务接口
 */
interface ReplyService {
    /**
     * 获取帖子回复
     */
    suspend fun getReplies(postId: String, page: Int, pageSize: Int): ReplyListResult

    /**
     * 创建回复
     */
    suspend fun createReply(postId: String, authorId: String, content: String, parentId: String?): ForumReply?

    /**
     * 更新回复
     */
    suspend fun updateReply(replyId: String, content: String): ForumReply?

    /**
     * 删除回复
     */
    suspend fun deleteReply(replyId: String): Boolean
}
