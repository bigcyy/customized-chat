package com.cyy.common.enums;

import lombok.Getter;

/**
 * 错误码的规范，采用 ABCDE 五位数错误码
 * 默认 00000 为成功
 *
 * A 位: 错误等级
 *  - A: 表示客户端错误
 *  - B: 表示服务端错误
 * BC 位：业务模块
 *  - 00：通用错误（未分类）
 *  - 01：用户相关
 *  - 02：设置相关
 *  - 03：知识库相关
 *  - 04：应用相关
 *  - 05：对话相关
 *
 * DE 位：具体错误类型
 *  - 00：未知错误
 *  - 01：缺少必要参数
 *  - 02：参数格式错误
 *  - 03：未授权访问
 *  - 04：资源不存在
 *  - 05：操作失败（具体原因待补充）
 *  - 06：请求超时
 *  - 07：资源已存在
 *  - 08：数据校验失败
 *
 * @author winter
 * @author CYY
 */
@Getter
public enum ErrorCode {

    SUCCESS("00000", "success"),
    CLIENT_ERROR("A0000","客户端错误"),
    SYSTEM_ERROR("B0000", "系统内部异常"),
    APPLICATION_NO_MODEL_CONFIG_ERROR("B0401","应用模型配置错误");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}