package com.devtool.component.core.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.UUIDClock;

import java.security.SecureRandom;

/**
 * 生成 UUID
 *
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2024-05-03
 */
public class UUIDGenerate {

    private UUIDGenerate() {}

    public static String next() {
        // v7
        return Generators.timeBasedEpochRandomGenerator(new SecureRandom(), new UUIDClock()).generate().toString();
    }
}
