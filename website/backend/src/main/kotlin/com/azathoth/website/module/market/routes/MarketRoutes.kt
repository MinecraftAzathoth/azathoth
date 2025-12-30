package com.azathoth.website.module.market.routes

import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.repository.MarketRepository
import com.azathoth.website.module.market.repository.ReviewRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureMarketRoutes() {
    val marketRepository = MarketRepository()
    val reviewRepository = ReviewRepository()

    routing {
        route("/api/market") {
            // ========== 公开接口 ==========

            // 搜索资源
            get("/resources") {
                val params = SearchParams(
                    keyword = call.parameters["keyword"],
                    type = call.parameters["type"],
                    license = call.parameters["license"],
                    minRating = call.parameters["minRating"]?.toDoubleOrNull(),
                    tags = call.parameters["tags"]?.split(",")?.filter { it.isNotBlank() },
                    sortBy = call.parameters["sortBy"] ?: "DOWNLOADS",
                    sortOrder = call.parameters["sortOrder"] ?: "DESC",
                    page = call.parameters["page"]?.toIntOrNull() ?: 1,
                    pageSize = call.parameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                )

                val (resources, totalCount) = marketRepository.searchResources(params)
                val totalPages = ((totalCount + params.pageSize - 1) / params.pageSize).toInt()

                call.respond(SearchResourcesResponse(
                    success = true,
                    resources = resources,
                    pagination = PaginationDTO(
                        page = params.page,
                        pageSize = params.pageSize,
                        totalCount = totalCount,
                        totalPages = totalPages
                    )
                ))
            }

            // 获取热门资源
            get("/popular") {
                val type = call.parameters["type"]
                val limit = call.parameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val resources = marketRepository.getPopularResources(type, limit)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // 获取最新资源
            get("/latest") {
                val type = call.parameters["type"]
                val limit = call.parameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val resources = marketRepository.getLatestResources(type, limit)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // 根据 ID 获取资源详情
            get("/resources/{resourceId}") {
                val resourceId = call.parameters["resourceId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ResourceResponse(success = false, error = ErrorDTO("INVALID_ID", "资源ID无效"))
                )

                val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ResourceResponse(success = false, error = ErrorDTO("INVALID_ID", "资源ID格式错误"))
                    )
                }

                val resource = marketRepository.getResourceById(uuid)
                if (resource != null) {
                    call.respond(ResourceResponse(success = true, resource = resource))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                    )
                }
            }

            // 根据 slug 获取资源详情
            get("/resources/slug/{slug}") {
                val slug = call.parameters["slug"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ResourceResponse(success = false, error = ErrorDTO("INVALID_SLUG", "Slug无效"))
                )

                val resource = marketRepository.getResourceBySlug(slug)
                if (resource != null) {
                    call.respond(ResourceResponse(success = true, resource = resource))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                    )
                }
            }

            // 获取资源评论
            get("/resources/{resourceId}/reviews") {
                val resourceId = call.parameters["resourceId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ReviewListResponse(
                        success = false,
                        reviews = emptyList(),
                        pagination = PaginationDTO(1, 20, 0, 0)
                    )
                )

                val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }

                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.parameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20

                val (reviews, totalCount) = reviewRepository.getReviews(uuid, page, pageSize)
                val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()

                call.respond(ReviewListResponse(
                    success = true,
                    reviews = reviews,
                    pagination = PaginationDTO(page, pageSize, totalCount, totalPages)
                ))
            }

            // 获取用户资源列表
            get("/users/{userId}/resources") {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val uuid = try { UUID.fromString(userId) } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }

                val resources = marketRepository.getUserResources(uuid)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // ========== 需要认证的接口 ==========

            authenticate("jwt") {
                // 创建资源
                post("/resources") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val userName = principal.payload.getClaim("userName")?.asString() ?: "Unknown"

                    val request = call.receive<CreateResourceRequestDTO>()
                    val uuid = UUID.fromString(userId)

                    val resource = marketRepository.createResource(uuid, userName, request)
                    if (resource != null) {
                        call.respond(HttpStatusCode.Created, ResourceResponse(success = true, resource = resource))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ResourceResponse(success = false, error = ErrorDTO("CREATE_FAILED", "创建资源失败"))
                        )
                    }
                }

                // 更新资源
                patch("/resources/{resourceId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@patch call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@patch call.respond(
                            HttpStatusCode.NotFound,
                            ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                        )
                    }
                    if (existing.authorId != userId) {
                        return@patch call.respond(
                            HttpStatusCode.Forbidden,
                            ResourceResponse(success = false, error = ErrorDTO("FORBIDDEN", "无权修改此资源"))
                        )
                    }

                    val request = call.receive<UpdateResourceRequestDTO>()
                    val updated = marketRepository.updateResource(uuid, request)

                    if (updated != null) {
                        call.respond(ResourceResponse(success = true, resource = updated))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ResourceResponse(success = false, error = ErrorDTO("UPDATE_FAILED", "更新资源失败"))
                        )
                    }
                }

                // 删除资源
                delete("/resources/{resourceId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@delete call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@delete call.respond(HttpStatusCode.NotFound)
                    }
                    if (existing.authorId != userId) {
                        return@delete call.respond(HttpStatusCode.Forbidden)
                    }

                    val deleted = marketRepository.deleteResource(uuid)
                    call.respond(SimpleResponse(success = deleted))
                }

                // 发布新版本 (multipart)
                post("/resources/{resourceId}/versions") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@post call.respond(HttpStatusCode.NotFound)
                    }
                    if (existing.authorId != userId) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    var version: String? = null
                    var changelog: String? = null
                    var minApiVersion: String? = null
                    var fileBytes: ByteArray? = null

                    val multipart = call.receiveMultipart()
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "version" -> version = part.value
                                    "changelog" -> changelog = part.value
                                    "minApiVersion" -> minApiVersion = part.value
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "file") {
                                    fileBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    if (version == null || changelog == null || minApiVersion == null || fileBytes == null) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            VersionResponse(success = false, error = ErrorDTO("MISSING_FIELDS", "缺少必要字段"))
                        )
                    }

                    // TODO: 上传文件到对象存储
                    val downloadUrl = "https://cdn.azathoth.dev/resources/$resourceId/$version/plugin.jar"

                    val request = PublishVersionRequestDTO(version!!, changelog!!, minApiVersion!!)
                    val versionDTO = marketRepository.publishVersion(uuid, request, downloadUrl, fileBytes!!.size.toLong())

                    if (versionDTO != null) {
                        call.respond(HttpStatusCode.Created, VersionResponse(success = true, version = versionDTO))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            VersionResponse(success = false, error = ErrorDTO("PUBLISH_FAILED", "发布版本失败"))
                        )
                    }
                }

                // 创建评论
                post("/resources/{resourceId}/reviews") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val userName = principal.payload.getClaim("userName")?.asString() ?: "Unknown"

                    val resourceId = call.parameters["resourceId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val resourceUuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    val userUuid = UUID.fromString(userId)

                    val request = call.receive<CreateReviewRequestDTO>()

                    if (request.rating !in 1..5) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ReviewResponse(success = false, error = ErrorDTO("INVALID_RATING", "评分必须在1-5之间"))
                        )
                    }

                    val review = reviewRepository.createReview(
                        resourceUuid, userUuid, userName, request.rating, request.content
                    )

                    if (review != null) {
                        call.respond(HttpStatusCode.Created, ReviewResponse(success = true, review = review))
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            ReviewResponse(success = false, error = ErrorDTO("ALREADY_REVIEWED", "您已评论过此资源"))
                        )
                    }
                }

                // 回复评论 (仅资源作者)
                post("/reviews/{reviewId}/reply") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val reviewId = call.parameters["reviewId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val reviewUuid = try { UUID.fromString(reviewId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证是否为资源作者
                    val authorId = reviewRepository.getReviewResourceAuthorId(reviewUuid)
                    if (authorId?.toString() != userId) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ReviewResponse(success = false, error = ErrorDTO("FORBIDDEN", "只有资源作者可以回复评论"))
                        )
                    }

                    val request = call.receive<ReplyReviewRequestDTO>()
                    val review = reviewRepository.replyToReview(reviewUuid, request.reply)

                    if (review != null) {
                        call.respond(ReviewResponse(success = true, review = review))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ReviewResponse(success = false, error = ErrorDTO("NOT_FOUND", "评论不存在"))
                        )
                    }
                }

                // 标记评论有帮助
                post("/reviews/{reviewId}/helpful") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val reviewId = call.parameters["reviewId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val reviewUuid = try { UUID.fromString(reviewId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    val userUuid = UUID.fromString(userId)

                    val success = reviewRepository.markHelpful(reviewUuid, userUuid)
                    call.respond(SimpleResponse(success = success))
                }
            }
        }
    }
}
