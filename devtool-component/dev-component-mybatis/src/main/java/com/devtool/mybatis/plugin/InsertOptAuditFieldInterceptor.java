package com.devtool.mybatis.plugin;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:xiongzihao_xzh@163.com">xzh</a>
 * @date 2024-04-24
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class InsertOptAuditFieldInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = statement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        // 在这里编写你的拦截逻辑，可以修改 SQL 语句
        System.out.println("Original SQL: " + originalSql);

        // 这里可以修改 SQL 语句，比如添加额外的条件
//        String modifiedSql = """
//                update tb_menu
//                SET menu_name = ?, update_by = 'xzhhhhhhhh'
//                where id=?
//                """;
        String modifiedSql = addStatement(originalSql);
        System.out.println("Modified SQL: " + modifiedSql);

        // 将修改后的 SQL 语句设置回去
        BoundSql newBoundSql = new BoundSql(statement.getConfiguration(), modifiedSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        MappedStatement newStatement = copyFromMappedStatement(statement, new BoundSqlSqlSource(newBoundSql));
        args[0] = newStatement;

        return invocation.proceed();
    }

    // 创建一个新的 MappedStatement 对象
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
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

    public  String addStatement(String sqlStr) throws Exception {
        Statement stmt = CCJSqlParserUtil.parse(sqlStr);
        if (stmt instanceof Update) {
            Update updateStatement = (Update) stmt;
            List<UpdateSet> updateSets = updateStatement.getUpdateSets();
            List<UpdateSet> addUpdateSetList = new ArrayList<>();

            Set<String> columnNameSet1 = updateSets.stream().map(UpdateSet::getColumns).flatMap(List::stream).map(Column::getColumnName).collect(Collectors.toSet());

            if(!columnNameSet1.contains("update_by")){
                addUpdateSetList.add(new UpdateSet(new Column("update_by"),new StringValue("xzh")));
            }
            if(!columnNameSet1.contains("update_time")){
                addUpdateSetList.add(new UpdateSet(new Column("update_time"),new StringValue(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"))));
            }

            for (UpdateSet updateSet : updateSets) {
                ExpressionList<Column> columnList = updateSet.getColumns();
                Set<String> columnNameSet = columnList.stream().map(Column::getColumnName).collect(Collectors.toSet());
                if(columnNameSet.contains("update_by")){
                    // mysql 只支持更新单列值，直接删除
                    updateSet.getColumns().removeFirst();
                    updateSet.getValues().removeFirst();
                    updateSet.add(new Column("update_by"),new StringValue("xzh"));
                }
                if(columnNameSet.contains("update_time")){
                    // mysql 只支持更新单列值，直接删除
                    updateSet.getColumns().removeFirst();
                    updateSet.getValues().removeFirst();
                    updateSet.add(new Column("update_time"),new StringValue(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss")));
                }
            }
            updateSets.addAll(addUpdateSetList);
        }
        String modifySql = stmt.toString();

        return modifySql;
    }
}
