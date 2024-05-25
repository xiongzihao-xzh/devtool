package com.devtool.mybatis.plugin;

import com.devtool.mybatis.constant.ColumnFieldConstant;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2024-04-24
 */
@Slf4j
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class InsertOptAuditFieldInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement statement = (MappedStatement) args[0];
            Object parameter = args[1];
            BoundSql boundSql = statement.getBoundSql(parameter);
            String originalSql = boundSql.getSql();

            // 在这里编写你的拦截逻辑，可以修改 SQL 语句
            log.info("originalSql:{}", originalSql);

            // 这里可以修改 SQL 语句，比如添加额外的条件
            //        String modifiedSql = """
            //                update tb_menu
            //                SET menu_name = ?, update_by = 'xzhhhhhhhh'
            //                where id=?
            //                """;
            String modifiedSql = addStatement(originalSql);
            log.info("modifiedSql:{}", modifiedSql);

            // 将修改后的 SQL 语句设置回去
            BoundSql newBoundSql = new BoundSql(
                statement.getConfiguration(),
                modifiedSql,
                boundSql.getParameterMappings(),
                boundSql.getParameterObject()
            );
            MappedStatement newStatement = copyFromMappedStatement(statement, new BoundSqlSqlSource(newBoundSql));
            args[0] = newStatement;
        } catch (Exception e) {
            log.error("mybatis intercept InsertOptAuditFieldInterceptor 自动填充字段出错: ", e);
        }

        return invocation.proceed();
    }

    // 创建一个新的 MappedStatement 对象
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
            ms.getConfiguration(),
            ms.getId(),
            newSqlSource,
            ms.getSqlCommandType()
        );
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.keyProperty(join(ms.getKeyProperties()));
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    private static class BoundSqlSqlSource implements SqlSource {

        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    private static String join(String[] strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string).append(",");
        }
        return builder.substring(0, builder.length() - 1);
    }

    public String addStatement(String sqlStr) throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(sqlStr);

        if (stmt instanceof Insert) {
            Insert insertStatement = (Insert) stmt;
            ExpressionList<Column> columns = insertStatement.getColumns();
            Values values = insertStatement.getValues();

            if (columns == null || values == null) {
                return stmt.toString();
            }

            // 获取 column name
            Set<String> columnNameSet = columns.stream().map(Column::getColumnName).collect(Collectors.toSet());

            if (!columnNameSet.contains(ColumnFieldConstant.CREATE_BY)) {
                columns.add(new Column(ColumnFieldConstant.CREATE_BY));
                values.addExpressions(new StringValue(getContextUsername()));
            }

            if (!columnNameSet.contains(ColumnFieldConstant.CREATE_TIME)) {
                columns.add(new Column(ColumnFieldConstant.CREATE_TIME));
                values.addExpressions(new StringValue(getNowDateFormat()));
            }
        } else if (stmt instanceof Update) {
            Update updateStatement = (Update) stmt;
            List<UpdateSet> updateSets = updateStatement.getUpdateSets();
            List<UpdateSet> addedUpdateSetList = new ArrayList<>();

            if (updateSets == null) {
                return stmt.toString();
            }

            // update set 的 column name
            Set<String> originalColumnNameSet = updateSets
                .stream()
                .map(UpdateSet::getColumns)
                .flatMap(List::stream)
                .map(Column::getColumnName)
                .collect(Collectors.toSet());

            if (!originalColumnNameSet.contains(ColumnFieldConstant.UPDATE_BY)) {
                // 不存在 update_by 列则插入 update_by 列到 set 语句后面
                addedUpdateSetList.add(
                    new UpdateSet(new Column(ColumnFieldConstant.UPDATE_BY), new StringValue(getContextUsername()))
                );
            }
            if (!originalColumnNameSet.contains(ColumnFieldConstant.UPDATE_TIME)) {
                // 不存在 update_time 列则插入 update_time 列到 set 语句后面
                addedUpdateSetList.add(
                    new UpdateSet(new Column(ColumnFieldConstant.UPDATE_TIME), new StringValue(getNowDateFormat()))
                );
            }

            for (UpdateSet updateSet : updateSets) {
                if (updateSet == null) {
                    return stmt.toString();
                }
                ExpressionList<Column> columnList = updateSet.getColumns();
                if (columnList == null) {
                    continue;
                }

                Set<String> columnNameSet = columnList.stream().map(Column::getColumnName).collect(Collectors.toSet());
                if (columnNameSet.contains(ColumnFieldConstant.UPDATE_BY)) {
                    // mysql 只支持更新单列值，直接删除
                    updateSet.getColumns().remove(0);
                    updateSet.getValues().remove(0);

                    updateSet.add(new Column(ColumnFieldConstant.UPDATE_BY), new StringValue(getContextUsername()));
                }
                if (columnNameSet.contains(ColumnFieldConstant.UPDATE_TIME)) {
                    // mysql 只支持更新单列值，直接删除
                    updateSet.getColumns().remove(0);
                    updateSet.getValues().remove(0);

                    updateSet.add(new Column(ColumnFieldConstant.UPDATE_TIME), new StringValue(getNowDateFormat()));
                }
            }
            updateSets.addAll(addedUpdateSetList);
        }

        return stmt.toString();
    }

    private String getContextUsername() {
        return "xzh";
    }

    private String getNowDateFormat() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(now);
    }
}
