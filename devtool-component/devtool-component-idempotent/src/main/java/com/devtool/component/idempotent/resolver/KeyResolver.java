package com.devtool.component.idempotent.resolver;

import com.devtool.component.idempotent.annotation.Idempotent;

/**
 * 
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2023/11/22
 */
public interface KeyResolver {
    
    String resolver(Idempotent idempotent);
}
