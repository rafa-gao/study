package mybatis.plugintest;

/**
 * @author rafa gao
 */


public class ExecutorImpl implements Executor {
    @Override
    public void query() {
        System.out.println("执行查询");
    }
}
