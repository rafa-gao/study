package mybatis.plugintest;

/**
 * @author rafa gao
 */


public class PluginTestMain {

    public static void main(String[] args) throws Exception {
        Container container = new Container();
        Executor executor = container.newExecutor();
        executor.query();

    }
}
